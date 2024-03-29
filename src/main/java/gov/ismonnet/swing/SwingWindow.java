package gov.ismonnet.swing;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import gov.ismonnet.bootstrap.Bootstrap;
import gov.ismonnet.lifecycle.LifeCycle;
import gov.ismonnet.lifecycle.LifeCycleService;
import gov.ismonnet.util.SneakyThrow;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

@AutoFactory
public class SwingWindow extends JFrame implements LifeCycle {

    private final LifeCycleService lifeCycleService;

    private JPanel currentScreen;

    private final MouseGrabHandler mouseGrabHandler;
    private final Cursor blankCursor;

    private final AtomicBoolean disposing = new AtomicBoolean(false);

    @Inject SwingWindow(@Provided @Bootstrap LifeCycleService lifeCycleService) {
        this.lifeCycleService = lifeCycleService;

        setTitle("SwingGame");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setSize(1200, 800);
        setMinimumSize(new Dimension(600, 400));
        setResizable(true);
        setLocationRelativeTo(null);

        mouseGrabHandler = new MouseGrabHandler(this);
        Toolkit.getDefaultToolkit().addAWTEventListener(mouseGrabHandler,
                AWTEvent.FOCUS_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
        Toolkit.getDefaultToolkit().addAWTEventListener(new FullscreenHandler(), AWTEvent.KEY_EVENT_MASK);
        // Hide cursor for this panel
        // Thanks to https://stackoverflow.com/a/10687248 and https://stackoverflow.com/a/1984117
        blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
                new Point(),
                "blank cursor");

        lifeCycleService.beforeStop(() -> setVisible(false));
        lifeCycleService.register(this);
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        disposing.set(true);
        dispose();
    }

    @Override
    public void dispose() {
        if(!disposing.getAndSet(true))
            SneakyThrow.runUnchecked(lifeCycleService::stop);
        super.dispose();
        disposing.set(false);
    }

    public void setScreen(JPanel newScreen) {
        if(newScreen == currentScreen)
            return;

        final JPanel oldScreen = currentScreen;
        currentScreen = newScreen;

        EventQueue.invokeLater(() -> {
            if(oldScreen != null)
                remove(oldScreen);

            add(newScreen);
            setContentPane(newScreen);
            setVisible(true);
        });
    }

    public JPanel getCurrentScreen() {
        return currentScreen;
    }


    public void setMouseGrabbed(boolean isMouseGrabbed) {
        mouseGrabHandler.isMouseGrabbed = isMouseGrabbed;
        setCursor(isMouseGrabbed ? blankCursor : Cursor.getDefaultCursor());
    }

    public boolean isMouseGrabbed() {
        return mouseGrabHandler.isMouseGrabbed;
    }

    static class MouseGrabHandler implements AWTEventListener {

        private final JFrame frame;
        private final Robot robot;

        private boolean isMouseGrabbed;

        MouseGrabHandler(JFrame frame) {
            this.frame = frame;
            this.robot = SneakyThrow.callUnchecked(Robot::new);
        }

        @Override
        public void eventDispatched(AWTEvent event) {
            if(event.getID() == MouseEvent.MOUSE_DRAGGED)
                mouseDragged((MouseEvent) event);
            else if(event.getID() == MouseEvent.MOUSE_MOVED)
                mouseMoved((MouseEvent) event);
            else if(event.getID() == FocusEvent.FOCUS_GAINED)
                focusGained((FocusEvent) event);
        }

        private void focusGained(FocusEvent e) {
            if(isMouseGrabbed)
                centerCursor();
        }

        private void centerCursor() {
            final Point onScreen = frame.getContentPane().getLocationOnScreen();
            robot.mouseMove(
                    (int) onScreen.getX() + frame.getWidth() / 2,
                    (int) onScreen.getY() + frame.getHeight() / 2);
        }

        private void mouseDragged(MouseEvent e) {
            onMouseMoved(e);
        }

        private void mouseMoved(MouseEvent e) {
            onMouseMoved(e);
        }

        private void onMouseMoved(MouseEvent e) {
            // Just if this window is focused
            if(!isMouseGrabbed || !frame.isFocused())
                return;
            // Thanks to https://stackoverflow.com/a/32159962
            // Moved by Robot, don't care
            if(e.getX() == frame.getWidth() / 2 && e.getY() == frame.getHeight() / 2)
                return;
            // Move the mouse back to the center
            centerCursor();
        }
    }

    class FullscreenHandler implements AWTEventListener {

        private boolean isFullScreen;
        private Rectangle bounds;

        @Override
        public void eventDispatched(AWTEvent event) {
            if(event.getID() == KeyEvent.KEY_PRESSED)
                keyPressed((KeyEvent) event);
        }

        private void keyPressed(KeyEvent e) {
            if(e.getKeyCode() != KeyEvent.VK_F11)
                return;

            isFullScreen = !isFullScreen;
            if(isFullScreen) {
                bounds = getBounds();

                SwingWindow.super.dispose();
                setUndecorated(true);
                setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
                setVisible(true);
            } else {
                SwingWindow.super.dispose();
                setUndecorated(false);
                setExtendedState(getExtendedState() & ~JFrame.MAXIMIZED_BOTH);
                setBounds(bounds);
                setVisible(true);
            }
        }
    }
}
