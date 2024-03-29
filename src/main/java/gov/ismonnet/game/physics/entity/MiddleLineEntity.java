package gov.ismonnet.game.physics.entity;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import dagger.Lazy;
import gov.ismonnet.game.physics.table.Table;
import gov.ismonnet.netty.core.NetService;
import gov.ismonnet.netty.packets.PuckPositionPacket;

import javax.inject.Inject;

@AutoFactory
public class MiddleLineEntity extends WallEntity {

    private final Table table;
    private final Lazy<PuckEntity> lazyPuck;

    private final NetService netService;

    private boolean sendResetPacket = false;

    @Inject MiddleLineEntity(float posX, float posY,
                             float width, float height,
                             @Provided Table table,
                             @Provided Lazy<PuckEntity> lazyPuck,
                             @Provided NetService netService) {
        super(posX, posY, width, height);

        this.table = table;
        this.lazyPuck = lazyPuck;
        this.netService = netService;
    }

    @Override
    public void tick() {
        super.tick();

        final PuckEntity puck = this.lazyPuck.get();

        final boolean collides = collidesWith(puck);
        if(collides && puck.getMotionX() > 0)
            netService.sendPacket(new PuckPositionPacket(
                    table.getWidth() + table.getWidth() - puck.getPosX(),
                    puck.getPosY(),
                    -puck.getMotionX(),
                    puck.getMotionY()));

        if (!collides && puck.getPosX() > table.getWidth()) {
            if(sendResetPacket)
                netService.sendPacket(new PuckPositionPacket(
                        table.getWidth() + table.getWidth() - puck.getPosX(),
                        puck.getPosY(),
                        -puck.getMotionX(),
                        puck.getMotionY()));
            sendResetPacket = false;

            puck.reset(table.getWidth() + puck.getRadius(), 0, 0, 0);
        } else {
            sendResetPacket = true;
        }
    }

    @Override
    public String toString() {
        return "MiddleLineEntity{} " + super.toString();
    }
}
