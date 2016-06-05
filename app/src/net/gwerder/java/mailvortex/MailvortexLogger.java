package net.gwerder.java.mailvortex;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;


public class MailvortexLogger extends Logger {

    static final String LINE_SEPARATOR = System.getProperty("line.separator");

    static {
        // remove all existing console handler
        Handler[] handlers = getGlobalLogger().getParent().getHandlers();
        if (handlers != null) {
            for (Handler h : handlers) {
                getGlobalLogger().getParent().removeHandler( h );
            }
        }

        // set log formater
        Handler console = new ConsoleHandler();
        console.setFormatter( new MyLogFormatter() );
        getGlobalLogger().getParent().addHandler( console );
    }

    private MailvortexLogger() {
        super( null, null );
    }

    public static void setGlobalLogLevel(Level l) {
        getGlobalLogger().setLevel( l );
    }

    public static Logger getGlobalLogger() {
        return LogManager.getLogManager().getLogger( Logger.GLOBAL_LOGGER_NAME );
    }
    
    static final class MyLogFormatter extends Formatter {


        @Override
        public String format(LogRecord record) {

            StringBuilder sb = new StringBuilder();

            sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()))
                .append(" ")
                .append(record.getLevel().getLocalizedName())
                .append(": ")
                .append("[")
                .append(Thread.currentThread().getName())
                .append("] ")
                .append(formatMessage(record))
                .append(LINE_SEPARATOR);

            if (record.getThrown() != null) {
                try {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    record.getThrown().printStackTrace(pw);
                    pw.close();
                    sb.append(sw.toString());
                } catch (Exception ex) {
                    assert true:"Never throw assertion"+ex;
                }
            }

            return sb.toString();
        }
    }
    
}