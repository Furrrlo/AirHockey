package gov.ismonnet.netty.server;

import gov.ismonnet.event.EventListener;
import gov.ismonnet.event.bus.BaseBus;
import gov.ismonnet.event.bus.WeakBus;
import gov.ismonnet.lifecycle.LifeCycle;
import gov.ismonnet.lifecycle.LifeCycleService;
import gov.ismonnet.netty.KeepAliveTimeout;
import gov.ismonnet.netty.Port;
import gov.ismonnet.netty.codecs.ByteStuffingDecoder;
import gov.ismonnet.netty.codecs.ByteStuffingEncoder;
import gov.ismonnet.netty.codecs.PacketDecoder;
import gov.ismonnet.netty.codecs.PacketEncoder;
import gov.ismonnet.netty.core.NetService;
import gov.ismonnet.netty.core.Packet;
import gov.ismonnet.netty.core.PacketIdService;
import gov.ismonnet.netty.exceptions.DelimiterDecoderException;
import gov.ismonnet.netty.exceptions.NetworkException;
import gov.ismonnet.netty.packets.DisconnectPacket;
import gov.ismonnet.netty.packets.KickPacket;
import gov.ismonnet.netty.packets.PingPacket;
import gov.ismonnet.netty.packets.PongPacket;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ServerNetService implements NetService, LifeCycle {

    private static final Logger LOGGER = LogManager.getLogger(ServerNetService.class);
//    private final static int SHUTDOWN_TIMEOUT = 5000;

    private static CompletableFuture<Void> SHUT_DOWN_FUTURE = CompletableFuture.completedFuture(null);

    private final LifeCycleService lifeCycleService;

    private final ServerBootstrap bootstrap;
    private final int port;

    private final BaseBus<Packet> delegateBus;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private CompletableFuture<Void> bindFuture;

    private CompletableFuture<Channel> clientFuture;
    private Channel clientChannel;

    private volatile boolean isStopped = false;

    @Inject ServerNetService(@Port int port,
                             @KeepAliveTimeout int keepAliveTimeout,
                             PacketIdService packetIdService,
                             LifeCycleService lifeCycleService) {
        this.port = port;
        this.lifeCycleService = lifeCycleService;

        this.delegateBus = new WeakBus<>();
        this.bootstrap = new ServerBootstrap()
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        // Decoders

                        ch.pipeline().addLast("timeout", new ReadTimeoutHandler(keepAliveTimeout, TimeUnit.MILLISECONDS));
                        ch.pipeline().addLast("framer", new ByteStuffingDecoder());
                        ch.pipeline().addLast("decoder", new PacketDecoder(packetIdService::getParserById));

                        // Encoders

                        ch.pipeline().addLast("frame_encoder", new ByteStuffingEncoder());
                        ch.pipeline().addLast("encoder", new PacketEncoder(packetIdService::getPacketId));

                        // Handlers

                        ch.pipeline().addLast("keep_alive_handler", new KeepAliveHandler());
                        ch.pipeline().addLast("packet_handler", new PacketHandler());
                    }
                })
                .localAddress(port);

        lifeCycleService.register(this);
    }

    @Override
    public void start() throws Exception {
        bindFuture = new CompletableFuture<>();

        LOGGER.trace("[Start] Waiting for previous socket to release...");
        SHUT_DOWN_FUTURE.get();
        LOGGER.trace("[Start] Binding new socket...");

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        try {
            clientFuture = new CompletableFuture<>();
            bootstrap.group(bossGroup, workerGroup)
                    .bind()
                    .sync()
                    .addListener(future -> {
                        if(future.isSuccess())
                            bindFuture.complete(null);
                        else
                            bindFuture.completeExceptionally(future.cause());
                    });

            clientChannel = clientFuture.get();
        } catch (Exception ex) {
            bindFuture.complete(null);
            throw ex;
        } finally {
            clientFuture = null;
        }
    }

    @Override
    public void stop() throws Exception {
        LOGGER.trace("[Stop] Waiting for previous socket to release...");
        SHUT_DOWN_FUTURE.get();
        LOGGER.trace("[Stop] Waiting for new socket to bind...");
        bindFuture.get();
        LOGGER.trace("[Stop] Stopping...");

        isStopped = true;

        if(clientFuture != null)
            clientFuture.cancel(true);

//        bossGroup.shutdownGracefully().await(SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS);
//        workerGroup.shutdownGracefully().await(SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS);
        final CompletableFuture<Void> future = new CompletableFuture<>();
        bossGroup.shutdownGracefully()
                .addListener(f0 -> workerGroup.shutdownGracefully()
                        .addListener(f -> {
                            if(f.isSuccess())
                                future.complete(null);
                            else
                                future.completeExceptionally(f.cause());
                        }));
        SHUT_DOWN_FUTURE = future;
    }

    @Override
    public CompletableFuture<Void> sendPacket(Packet packet) {
        LOGGER.trace("Sending packet {}", packet);

        final CompletableFuture<Void> future = new CompletableFuture<>();
        clientChannel.writeAndFlush(packet).addListener(channelFuture -> {
            if(channelFuture.isSuccess())
                future.complete(null);
            else
                future.completeExceptionally(channelFuture.cause());
        });
        return future;
    }

    @ChannelHandler.Sharable
    private final class KeepAliveHandler extends SimpleChannelInboundHandler<Packet> {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            super.channelRead(ctx, msg);
            ctx.fireChannelRead(msg); // Make the packet go through the pipeline
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Packet msg) {
            if(msg instanceof PingPacket)
                ctx.write(new PongPacket());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            if(cause instanceof DelimiterDecoderException) {
                LOGGER.error("Exception while framing packets (Sender: {})", ctx, cause);

            } else if(cause instanceof NetworkException) {
                LOGGER.error("Exception while decoding packets (Sender: {})", ctx, cause);

            } else  {
                if(cause instanceof ReadTimeoutException)
                    LOGGER.error("Connection timed out (Ctx: {})", ctx, cause);
                else
                    LOGGER.error("Uncaught exception inside the Netty pipeline (Ctx: {}) {}", ctx, cause);
                ctx.close();
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {

            if(clientChannel != null) {
                ctx.writeAndFlush(new KickPacket("A player is already connected")).sync();
                ctx.close();
            }

            if(clientFuture != null)
                clientFuture.complete(ctx.channel());
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            LOGGER.trace("Channel inactive");
            super.channelInactive(ctx);

            if(!isStopped)
                lifeCycleService.stop();
        }
    }

    @ChannelHandler.Sharable
    private final class PacketHandler extends SimpleChannelInboundHandler<Packet> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Packet msg) {

            if(msg instanceof DisconnectPacket)
                ctx.close();

            LOGGER.trace("Handle packet {}", msg);
            post(msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
            super.channelReadComplete(ctx);
        }
    }

    // Delegate event bus

    @Override
    public void register(EventListener<? extends Packet> listener) {
        delegateBus.register(listener);
    }

    @Override
    public void registerObj(Object obj) {
        delegateBus.registerObj(obj);
    }

    @Override
    public void registerObj(Object obj, Class<? extends Packet>... events) {
        delegateBus.registerObj(obj, events);
    }

    @Override
    public void unregister(EventListener<? extends Packet> listener) {
        delegateBus.unregister(listener);
    }

    @Override
    public void unregisterObj(Object obj) {
        delegateBus.unregisterObj(obj);
    }

    @Override
    public void unregisterObj(Object obj, Class<? extends Packet>... events) {
        delegateBus.unregisterObj(obj, events);
    }

    @Override
    public Packet post(Packet event) {
        return delegateBus.post(event);
    }
}
