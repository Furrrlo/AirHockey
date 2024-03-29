package gov.ismonnet.swing;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

public class SwingGraphics extends Graphics2D {

    private final Graphics2D g2d;

    public SwingGraphics(Graphics2D g2d) {
        this.g2d = g2d;
    }

    // Aligned strings

    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
    private VerticalAlignment verticalAlignment = VerticalAlignment.BOTTOM;

    @Override
    public void drawString(String str, int x, int y) {
        drawString(str, (float) x, (float) y);
    }

    @Override
    public void drawString(String str, float x, float y) {

        final FontMetrics fontMetrics = g2d.getFontMetrics();

        switch (horizontalAlignment) {
            case LEFT:
                break;
            case RIGHT:
                x -= fontMetrics.stringWidth(str);
                break;
            case CENTER:
                x -= (fontMetrics.stringWidth(str) / 2F);
                break;
        }

        switch (verticalAlignment) {
            case BOTTOM:
                break;
            case TOP:
                y = y - fontMetrics.getLeading() + fontMetrics.getAscent();
                break;
            case CENTER:
                y = y - fontMetrics.getHeight() / 2F + fontMetrics.getAscent();
                break;
        }

        g2d.drawString(str, x, y);
    }

    public void textAlign(HorizontalAlignment horizontal, VerticalAlignment vertical) {
        this.horizontalAlignment = horizontal;
        this.verticalAlignment = vertical;
    }

    public void textSize(float textSize) {
        g2d.setFont(g2d.getFont().deriveFont(textSize));
    }

    public enum HorizontalAlignment {
        LEFT, RIGHT, CENTER
    }

    public enum VerticalAlignment {
        TOP, BOTTOM, CENTER
    }

    // Bordered methods

    public void fillBorderedRect(int x, int y,
                                 int width, int height,
                                 Color border, Color inside) {
        setColor(inside);
        fillRect(x, y, width, height);
        setColor(border);
        drawRect(x, y, width, height);
    }

    public void fillBorderedRect(float x, float y,
                                 float width, float height,
                                 Color border, Color inside) {
        setColor(inside);
        fillRect(x, y, width, height);
        setColor(border);
        drawRect(x, y, width, height);
    }

    public void fillBorderedRoundRect(int x, int y,
                                      int width, int height,
                                      int diameter,
                                      Color border, Color inside) {
        setColor(inside);
        fillRoundRect(x, y, width, height, diameter, diameter);
        setColor(border);
        drawRoundRect(x, y, width, height, diameter, diameter);
    }

    public void fillBorderedRoundRect(float x, float y,
                                      float width, float height,
                                      float diameter,
                                      Color border, Color inside) {
        setColor(inside);
        fillRoundRect(x, y, width, height, diameter, diameter);
        setColor(border);
        drawRoundRect(x, y, width, height, diameter, diameter);
    }

    public void fillBorderedCircle(int centerX, int centerY, int radius,
                                   Color border, Color inside) {
        setColor(inside);
        fillCircle(centerX, centerY, radius);
        setColor(border);
        drawCircle(centerX, centerY, radius);
    }

    public void fillBorderedCircle(float centerX, float centerY, float radius,
                                   Color border, Color inside) {
        setColor(inside);
        fillCircle(centerX, centerY, radius);
        setColor(border);
        drawCircle(centerX, centerY, radius);
    }

    // Circle methods

    public void drawCircle(int centerX, int centerY, int radius) {
        g2d.drawOval(centerX - radius, centerY - radius, 2 * radius, 2 * radius);
    }

    public void fillCircle(int centerX, int centerY, int radius) {
        g2d.fillOval(centerX - radius, centerY - radius, 2 * radius, 2 * radius);
    }

    public void drawCircle(float centerX, float centerY, float radius) {
        drawOval(centerX - radius, centerY - radius, 2 * radius, 2 * radius);
    }

    public void fillCircle(float centerX, float centerY, float radius) {
        fillOval(centerX - radius, centerY - radius, 2 * radius, 2 * radius);
    }

    // Float overloads

    private static final ThreadLocal<Path2D.Float> LINE = ThreadLocal.withInitial(Path2D.Float::new);
    private static final ThreadLocal<Rectangle2D.Float> RECT = ThreadLocal.withInitial(Rectangle2D.Float::new);
    private static final ThreadLocal<RoundRectangle2D.Float> ROUND_RECT = ThreadLocal.withInitial(RoundRectangle2D.Float::new);
    private static final ThreadLocal<Ellipse2D.Float> OVAL = ThreadLocal.withInitial(Ellipse2D.Float::new);
    private static final ThreadLocal<Arc2D.Float> ARC = ThreadLocal.withInitial(Arc2D.Float::new);


    public void drawLine(float x1, float y1, float x2, float y2) {
        final Path2D.Float shape = LINE.get();
        shape.reset();
        shape.moveTo(x1, y1);
        shape.lineTo(x2, y2);
        draw(shape);
    }

    public void fillRect(float x, float y, float width, float height) {
        final Rectangle2D.Float shape = RECT.get();
        shape.setRect(x, y, width, height);
        fill(shape);
    }

    public void drawRect(float x, float y, float width, float height) {
        final Rectangle2D.Float shape = RECT.get();
        shape.setRect(x, y, width, height);
        draw(shape);
    }

    public void drawRoundRect(float x, float y,
                              float width, float height,
                              float arcWidth, float arcHeight) {
        final RoundRectangle2D.Float shape = ROUND_RECT.get();
        shape.setRoundRect(x, y, width, height, arcWidth, arcHeight);
        draw(shape);
    }

    public void fillRoundRect(float x, float y,
                              float width, float height,
                              float arcWidth, float arcHeight) {
        final RoundRectangle2D.Float shape = ROUND_RECT.get();
        shape.setRoundRect(x, y, width, height, arcWidth, arcHeight);
        fill(shape);
    }

    public void drawOval(float x, float y, float width, float height) {
        final Ellipse2D.Float shape = OVAL.get();
        shape.setFrame(x, y, width, height);
        draw(shape);
    }

    public void fillOval(float x, float y, float width, float height) {
        final Ellipse2D.Float shape = OVAL.get();
        shape.setFrame(x, y, width, height);
        fill(shape);
    }

    public void drawArc(float x, float y, float width, float height, float startAngle, float arcAngle) {
        final Arc2D.Float shape = ARC.get();
        shape.setArc(x, y, width, height, startAngle, arcAngle, Arc2D.OPEN);
        draw(shape);
    }

    public void fillArc(float x, float y, float width, float height, float startAngle, float arcAngle) {
        final Arc2D.Float shape = ARC.get();
        shape.setArc(x, y, width, height, startAngle, arcAngle, Arc2D.PIE);
        draw(shape);
    }

    // Thanks to https://gamedev.stackexchange.com/a/45048

    public boolean drawImage(Image img, float x, float y, ImageObserver observer) {
        translate(x, y);
        try {
            return drawImage(img, 0, 0, observer);
        } finally {
            translate(-x, -y);
        }
    }

    public boolean drawImage(Image img, float x, float y, int width, int height, ImageObserver observer) {
        translate(x, y);
        try {
            return drawImage(img, 0, 0, width, height, observer);
        } finally {
            translate(-x, -y);
        }
    }

    public boolean drawImage(Image img, float x, float y, float width, float height, ImageObserver observer) {
        final double wScale = width / (int) width;
        final double hScale = height / (int) height;

        translate(x, y);
        scale(wScale, hScale);
        try {
            return drawImage(img, 0, 0, (int) width, (int) height, observer);
        } finally {
            scale(1 / wScale, 1 / hScale);
            translate(-x, -y);
        }
    }


    // Graphics2D delegate

    @Override
    public void draw3DRect(int x, int y, int width, int height, boolean raised) {
        g2d.draw3DRect(x, y, width, height, raised);
    }

    @Override
    public void fill3DRect(int x, int y, int width, int height, boolean raised) {
        g2d.fill3DRect(x, y, width, height, raised);
    }

    @Override
    public void draw(Shape s) {
        g2d.draw(s);
    }

    @Override
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        return g2d.drawImage(img, xform, obs);
    }

    @Override
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        g2d.drawImage(img, op, x, y);
    }

    @Override
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        g2d.drawRenderedImage(img, xform);
    }

    @Override
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        g2d.drawRenderableImage(img, xform);
    }

    @Override
    public void fill(Shape s) {
        g2d.fill(s);
    }

    @Override
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        return g2d.hit(rect, s, onStroke);
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return g2d.getDeviceConfiguration();
    }

    @Override
    public void setComposite(Composite comp) {
        g2d.setComposite(comp);
    }

    @Override
    public void setPaint(Paint paint) {
        g2d.setPaint(paint);
    }

    @Override
    public void setStroke(Stroke s) {
        g2d.setStroke(s);
    }

    @Override
    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {
        g2d.setRenderingHint(hintKey, hintValue);
    }

    @Override
    public Object getRenderingHint(RenderingHints.Key hintKey) {
        return g2d.getRenderingHint(hintKey);
    }

    @Override
    public void setRenderingHints(Map<?, ?> hints) {
        g2d.setRenderingHints(hints);
    }

    @Override
    public void addRenderingHints(Map<?, ?> hints) {
        g2d.addRenderingHints(hints);
    }

    @Override
    public RenderingHints getRenderingHints() {
        return g2d.getRenderingHints();
    }

    @Override
    public void translate(int x, int y) {
        g2d.translate(x, y);
    }

    @Override
    public void translate(double tx, double ty) {
        g2d.translate(tx, ty);
    }

    @Override
    public void rotate(double theta) {
        g2d.rotate(theta);
    }

    @Override
    public void rotate(double theta, double x, double y) {
        g2d.rotate(theta, x, y);
    }

    @Override
    public void scale(double sx, double sy) {
        g2d.scale(sx, sy);
    }

    @Override
    public void shear(double shx, double shy) {
        g2d.shear(shx, shy);
    }

    @Override
    public void transform(AffineTransform Tx) {
        g2d.transform(Tx);
    }

    @Override
    public void setTransform(AffineTransform Tx) {
        g2d.setTransform(Tx);
    }

    @Override
    public AffineTransform getTransform() {
        return g2d.getTransform();
    }

    @Override
    public Paint getPaint() {
        return g2d.getPaint();
    }

    @Override
    public Composite getComposite() {
        return g2d.getComposite();
    }

    @Override
    public void setBackground(Color color) {
        g2d.setBackground(color);
    }

    @Override
    public Color getBackground() {
        return g2d.getBackground();
    }

    @Override
    public Stroke getStroke() {
        return g2d.getStroke();
    }

    @Override
    public void clip(Shape s) {
        g2d.clip(s);
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        return g2d.getFontRenderContext();
    }

    @Override
    public Graphics create() {
        return g2d.create();
    }

    @Override
    public Graphics create(int x, int y, int width, int height) {
        return g2d.create(x, y, width, height);
    }

    @Override
    public Color getColor() {
        return g2d.getColor();
    }

    @Override
    public void setColor(Color c) {
        g2d.setColor(c);
    }

    @Override
    public void setPaintMode() {
        g2d.setPaintMode();
    }

    @Override
    public void setXORMode(Color c1) {
        g2d.setXORMode(c1);
    }

    @Override
    public Font getFont() {
        return g2d.getFont();
    }

    @Override
    public void setFont(Font font) {
        g2d.setFont(font);
    }

    @Override
    public FontMetrics getFontMetrics() {
        return g2d.getFontMetrics();
    }

    @Override
    public FontMetrics getFontMetrics(Font f) {
        return g2d.getFontMetrics(f);
    }

    @Override
    public Rectangle getClipBounds() {
        return g2d.getClipBounds();
    }

    @Override
    public void clipRect(int x, int y, int width, int height) {
        g2d.clipRect(x, y, width, height);
    }

    @Override
    public void setClip(int x, int y, int width, int height) {
        g2d.setClip(x, y, width, height);
    }

    @Override
    public Shape getClip() {
        return g2d.getClip();
    }

    @Override
    public void setClip(Shape clip) {
        g2d.setClip(clip);
    }

    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        g2d.copyArea(x, y, width, height, dx, dy);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        g2d.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
        g2d.fillRect(x, y, width, height);
    }

    @Override
    public void drawRect(int x, int y, int width, int height) {
        g2d.drawRect(x, y, width, height);
    }

    @Override
    public void clearRect(int x, int y, int width, int height) {
        g2d.clearRect(x, y, width, height);
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        g2d.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        g2d.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public void drawOval(int x, int y, int width, int height) {
        g2d.drawOval(x, y, width, height);
    }

    @Override
    public void fillOval(int x, int y, int width, int height) {
        g2d.fillOval(x, y, width, height);
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        g2d.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        g2d.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        g2d.drawPolyline(xPoints, yPoints, nPoints);
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        g2d.drawPolygon(xPoints, yPoints, nPoints);
    }

    @Override
    public void drawPolygon(Polygon p) {
        g2d.drawPolygon(p);
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        g2d.fillPolygon(xPoints, yPoints, nPoints);
    }

    @Override
    public void fillPolygon(Polygon p) {
        g2d.fillPolygon(p);
    }

    @Override
    public void drawChars(char[] data, int offset, int length, int x, int y) {
        g2d.drawChars(data, offset, length, x, y);
    }

    @Override
    public void drawBytes(byte[] data, int offset, int length, int x, int y) {
        g2d.drawBytes(data, offset, length, x, y);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return g2d.drawImage(img, x, y, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        return g2d.drawImage(img, x, y, width, height, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        return g2d.drawImage(img, x, y, bgcolor, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        return g2d.drawImage(img, x, y, width, height, bgcolor, observer);
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        return g2d.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        return g2d.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        g2d.drawString(iterator, x, y);
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        g2d.drawString(iterator, x, y);
    }

    @Override
    public void drawGlyphVector(GlyphVector g, float x, float y) {
        g2d.drawGlyphVector(g, x, y);
    }

    @Override
    public void dispose() {
        g2d.dispose();
    }

    @Override
    public void finalize() {
        g2d.finalize();
    }

    @Override
    public String toString() {
        return g2d.toString();
    }

    @Override
    @Deprecated
    public Rectangle getClipRect() {
        return g2d.getClipRect();
    }

    @Override
    public boolean hitClip(int x, int y, int width, int height) {
        return g2d.hitClip(x, y, width, height);
    }

    @Override
    public Rectangle getClipBounds(Rectangle r) {
        return g2d.getClipBounds(r);
    }
}
