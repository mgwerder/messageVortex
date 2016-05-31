package net.gwerder.java.mailvortex;

import java.util.logging.Logger;
import java.util.logging.Level;
 
public abstract class MailVortex {

    private static final Logger LOGGER;
    static {
        LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    private MailVortex() {
        super();
    }

    public static int main(String[] args) {
      if(args!=null && args.length>0 && "--help".equals(args[0])) {
        LOGGER.log(Level.INFO, "MailVortex V"+Version.getBuild());
      }    
      return 0;
    }
}