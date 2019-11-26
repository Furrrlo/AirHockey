package gov.ismonnet.game.physics.entity;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import dagger.Lazy;
import gov.ismonnet.event.EventListener;
import gov.ismonnet.event.Listener;
import gov.ismonnet.event.listeners.SyncListener;
import gov.ismonnet.game.physics.table.Table;
import gov.ismonnet.netty.core.NetService;
import gov.ismonnet.netty.packets.PuckPositionPacket;

import javax.inject.Inject;
import java.util.Set;

@AutoFactory
public class PuckEntity extends CircleEntity {

    private static final float MOTION_STEP = 0.1f;
    private static final float MOTION_CAP = 30F;

    private final Table table;

    @Inject PuckEntity(float startX, float startY,
                       float radius,
                       float motionX, float motionY,
                       @Provided Table table,
                       @Provided Lazy<Set<Entity>> collidingEntitiesLazy,
                       @Provided NetService netService) {
        super(startX, startY, radius, collidingEntitiesLazy);

        this.table = table;

        this.posX = startX;
        this.posY = startY;

        this.motionX = motionX;
        this.motionY = motionY;

        netService.registerObj(this);
    }

    @Override
    public void tick() {
        // Using the theorem of similar triangles
        // cap the max motion
        {
            final float motion = (float) Math.sqrt(motionX * motionX + motionY * motionY);
            if(motion != 0) {
                final float cappedMotion = Math.min(motion, MOTION_CAP);
                // motion : cappedMotion = motionX : x
                motionX = motionX * cappedMotion / motion;
                motionY = motionY * cappedMotion / motion;
            }
        }

        setPosX(getPosX() + motionX);
        setPosY(getPosY() + motionY);

        // Calculate the motion - 0.1F

        final float motion = (float) Math.sqrt(motionX * motionX + motionY * motionY);
        if(motion != 0) {
            final float newMotion = Math.max(0, motion - MOTION_STEP);
            // motion : newMotion = motionX : x
            motionX = motionX * newMotion / motion;
            motionY = motionY * newMotion / motion;
        }
    }

    @Override
    protected void setPosY(float posYIn) {
        super.setPosY(posYIn);

        if(posY - radius < 0)
            posY = radius;
        if(posY + radius > table.getHeight())
            posY = table.getHeight() - radius;
    }

    @Listener
    protected EventListener<PuckPositionPacket> onPuckPos = new SyncListener<>(packet -> {
        this.posX = packet.getPosX();
        this.posY = packet.getPosY();
        this.motionX = packet.getMotionX();
        this.motionY = packet.getMotionY();
    });

    public void reset(float posX, float posY, float motionX, float motionY) {
        this.posX = posX;
        this.posY = posY;
        this.motionX = motionX;
        this.motionY = motionY;
    }

    @Override
    public String toString() {
        return "PuckEntity{} " + super.toString();
    }
}
