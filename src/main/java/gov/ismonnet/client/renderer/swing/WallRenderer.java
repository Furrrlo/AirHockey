package gov.ismonnet.client.renderer.swing;

import gov.ismonnet.client.entity.WallEntity;
import gov.ismonnet.client.renderer.Renderer;

import javax.inject.Inject;
import java.awt.*;

class WallRenderer implements Renderer<SwingRenderContext, WallEntity> {

    @Inject WallRenderer() {}

    @Override
    public void render(SwingRenderContext ctx, WallEntity toRender) {
        ctx.setColor(Color.yellow);
        ctx.fillRect(
                (int) toRender.getPosX(),
                (int) toRender.getPosY(),
                (int) toRender.getWidth(),
                (int) toRender.getHeight());
    }
}
