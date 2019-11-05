package gov.ismonnet.client.renderer.swing;

import gov.ismonnet.client.entity.DiskEntity;
import gov.ismonnet.client.renderer.Renderer;

import javax.inject.Inject;
import java.awt.*;

class DiskRenderer implements Renderer<SwingRenderContext, DiskEntity> {

    @Inject DiskRenderer() {}

    @Override
    public void render(SwingRenderContext ctx, DiskEntity toRender) {
        ctx.fillBorderedCircle(
                (int) toRender.getPosX(),
                (int) toRender.getPosY(),
                (int) toRender.getRadius(),
                Color.black, Color.red);
    }
}
