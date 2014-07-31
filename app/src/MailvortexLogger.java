package net.gwerder.java.mailvortex;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.text.SimpleDateFormat;
import java.util.logging.Handler;
import java.util.logging.ConsoleHandler;


public class MailvortexLogger extends Logger {

    static final String LINE_SEPARATOR = System.getProperty("line.separator");
    static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    static final class MyLogFormatter extends Formatter {


        @Override
        public String format(LogRecord record) {
            
            StringBuilder sb = new StringBuilder();

            sb.append(SDF.format(new Date()))
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

    static {
        // remove all existing console handler
        Handler[] handlers = getGlobalLogger().getParent().getHandlers();
        if (handlers!=null) {
            for(Handler h:handlers) {
                getGlobalLogger().getParent().removeHandler(h);
            }
        }

        // set log formater
        Handler console = new ConsoleHandler();
        console.setFormatter(new MyLogFormatter());
        getGlobalLogger().getParent().addHandler(console);
    }    
    

    private MailvortexLogger() {
        super(null,null);
    }
    
    public static void setGlobalLogLevel(Level l) {
        getGlobalLogger().getParent().setLevel(l); 
    }
    
    public static Logger getGlobalLogger() {
        return LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME);
    }
    
}