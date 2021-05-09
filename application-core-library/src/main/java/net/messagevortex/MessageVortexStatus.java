package net.messagevortex;

// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// ************************************************************************************

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public class MessageVortexStatus {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private static Image image;

  static {
    InputStream res = ClassLoader.getSystemResourceAsStream("images/MessageVortexLogo_32.png");
    try {
      image = ImageIO.read(res);
    } catch (IOException ioe) {
      LOGGER.log(Level.INFO, "error while loading try icon", ioe);
    }
  }

  private static TrayIcon trayIcon = null;

  static {
    if (SystemTray.isSupported()) {
      if (trayIcon == null) {
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
                LOGGER.log(Level.INFO, "got event " + e);
                try {
                  Thread.sleep(1000);
                } catch (InterruptedException ie) {
                  // safe to ignore
                }
                LOGGER.log(Level.INFO, "displaying event " + e);
                displayMessage("Action", "Some action performed");
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

  /***
   * <p>Displays a ballon message at the tray icon.</p>
   *
   * @param title   the title of the message
   * @param message the message to be displayed
   */
  public static synchronized void displayMessage(final String title, final String message) {
    if (SystemTray.isSupported()) {
      LOGGER.log(Level.INFO, "(" + title + ")" + message);
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          trayIcon.displayMessage("MessageVortex" + (title != null ? " " + title : ""), message,
                  TrayIcon.MessageType.INFO);
        }
      });
    }
  }

}
