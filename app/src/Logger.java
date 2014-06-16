package net.gwerder.java.mailvortex;

import org.apache.log4j.*;

public class Logger
{
  private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getRootLogger();
  
  static 
  {
    try 
	{
      //SimpleLayout layout = new SimpleLayout();
      //ConsoleAppender consoleAppender = new ConsoleAppender( layout );
      //logger.addAppender( consoleAppender );
      //FileAppender fileAppender = new FileAppender( layout, "logs/mailvortex.log", false );
      //logger.addAppender( fileAppender );
      // ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF:
      //logger.setLevel( Level.WARN );
    } catch( Exception ex ) 
	{
      System.out.println( ex );
    }
    logger.debug( "Meine Debug-Meldung" );
    logger.info(  "Meine Info-Meldung"  );
    logger.warn(  "Meine Warn-Meldung"  );
    logger.error( "Meine Error-Meldung" );
    logger.fatal( "Meine Fatal-Meldung" );
  }

  public static org.apache.log4j.Logger getLogger() 
  {
	return logger;
  }

}
