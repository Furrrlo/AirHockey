package gov.ismonnet.bootstrap.swing;

import gov.ismonnet.bootstrap.DefaultPort;
import gov.ismonnet.bootstrap.ServerBootstrapService;
import gov.ismonnet.swing.BackgroundColor;
import gov.ismonnet.swing.SwingWindow;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;

public class SwingServerBootstrapService extends SwingLoadingScreen implements ServerBootstrapService {

    private final int port;
    private final SwingWindow window;

    @Inject SwingServerBootstrapService(@DefaultPort int port,
                                        SwingWindow window,
                                        @BackgroundColor Color backgroundColor,
                                        ImageIcon luca) {
        super(backgroundColor, luca, "Aspettando che l'altro giocatore si connetta...");

        this.port = port;
        this.window = window;
    }

    @Override
    public int choosePort() {
        return port;
    }

    @Override
    public void startWaiting() {
        window.setScreen(this);
    }

    @Override
    public void stopWaiting() {
    }
}
