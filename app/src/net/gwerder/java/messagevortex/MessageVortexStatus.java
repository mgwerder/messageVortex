package net.gwerder.java.messagevortex;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

/**
 * Created by Martin on 15.04.2018.
 */
public class MessageVortexStatus {

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    private static Image image;
    static {
        InputStream res = ClassLoader.getSystemResourceAsStream( "images/MessageVortexLogo_32.png" );
        try {
            image = ImageIO.read( res );
        } catch (IOException ioe) {
            LOGGER.log( Level.INFO, "error while loading try icon", ioe );
        }
    }

    private static TrayIcon trayIcon = null;
    static {
        if (SystemTray.isSupported()) {
            if( trayIcon == null ) {
                /* Use an appropriate Look and Feel */
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                    //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                } catch (UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                } catch (InstantiationException ex) {
                    ex.printStackTrace();
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
                /* Turn off metal's use of bold fonts */
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                //Schedule a job for the event-dispatching thread to add tryicon
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        SystemTray tray = SystemTray.getSystemTray();
                        trayIcon = new TrayIcon(image, "MessageVortex");
                        trayIcon.setImageAutoSize(true);
                        trayIcon.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                LOGGER.log(Level.INFO, "got event " + e.toString());
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException ie) {
                                    // safe to ignore
                                }
                                LOGGER.log(Level.INFO, "displaying event " + e.toString());
                                displayMessage( "Action", "Some action performed" );
                            }
                        });

                        try {
                            tray.add(trayIcon);
                        } catch (AWTException e) {
                            System.err.println("TrayIcon could not be added.");
                        }
                    }
                });
                           }
        }
    }

    public static synchronized void displayMessage( final String title, final String message ) {
        if (SystemTray.isSupported()) {
            LOGGER.log(Level.INFO, "(" + title + ")" + message);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    trayIcon.displayMessage( "MessageVortex" + (title != null ? " " + title : ""), message, TrayIcon.MessageType.INFO );
                }
            });
        }
    }

}
