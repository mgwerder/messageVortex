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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class MessageVortexLogger extends Logger {

  static final String LINE_SEPARATOR = System.getProperty("line.separator");

  static final Handler consoleHandler = new ConsoleHandler();

  static {
    // remove all existing console handler
    Handler[] handlers = getGlobalLogger().getParent().getHandlers();
    if (handlers != null) {
      for (Handler h : handlers) {
        getGlobalLogger().getParent().removeHandler(h);
      }
    }

    // set log formater
    consoleHandler.setFormatter(new MyLogFormatter());
    getGlobalLogger().getParent().addHandler(consoleHandler);
  }

  private MessageVortexLogger() {
    super(null, null);
  }

  public static void setGlobalLogLevel(Level l) {
    consoleHandler.setLevel(l);
    getGlobalLogger().getParent().setLevel(l);
  }

  public static Logger getGlobalLogger() {
    return LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME);
  }

  static final class MyLogFormatter extends Formatter {


    @Override
    public String format(LogRecord record) {

      StringBuilder sb = new StringBuilder();

      sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
              .append(' ')
              .append(record.getLevel().getLocalizedName())
              .append(": ")
              .append('[')
              .append(Thread.currentThread().getName())
              .append("] ")
              .append(formatMessage(record))
              .append(LINE_SEPARATOR);

      //noinspection ThrowableResultOfMethodCallIgnored
      if (record.getThrown() != null) {
        try {
          StringWriter sw = new StringWriter();
          PrintWriter pw = new PrintWriter(sw);
          //noinspection ThrowableResultOfMethodCallIgnored
          record.getThrown().printStackTrace(pw);
          pw.close();
          sb.append(sw);
        } catch (Exception ex) {
          assert true : "Never throw assertion" + ex;
        }
      }

      return sb.toString();
    }
  }

  public static void flush() {
    consoleHandler.flush();
  }

}
