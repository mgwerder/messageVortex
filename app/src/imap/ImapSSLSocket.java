package net.gwerder.java.mailvortex.imap;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.util.concurrent.TimeoutException;
 
/**
 * @author Martin Gwerder
 */
public class ImapSSLSocket {

	private static final Logger LOGGER;
	static {
		LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
		LOGGER.setLevel(Level.FINEST);
	}
	
	/**
     * Selftest function for IMAP server.
     *
     * @param args      ignored commandline arguments
     ***/
    public static void main(String[] args) throws Exception {
		LOGGER.getParent().setLevel(Level.FINEST);
		boolean encrypted=false;
        ImapServer s=new ImapServer(0,encrypted);
        ImapClient c=new ImapClient("localhost",s.getPort(),encrypted);
		LOGGER.log(Level.INFO,"Sending commands");
		try{ 
			String command = ImapLine.getNextTag()+" CAPABILITY";
			LOGGER.log(Level.FINEST,"IMAP-> C: "+command);
			for(String v:c.sendCommand(command))  { 
				LOGGER.log(Level.FINEST,"IMAP<- C: "+v); 
			} 
		} catch(Exception e) {
			LOGGER.log(Level.SEVERE,"Error while sending CAPABILITY",e);
		}
		
		try{ 
			String command = ImapLine.getNextTag()+" LOGIN user passwort";
			LOGGER.log(Level.FINEST,"IMAP-> C: "+command);
			for(String v:c.sendCommand(command)) { 
				LOGGER.log(Level.FINEST,"IMAP<- C: "+v); 
			}
		} catch(TimeoutException e) {
			LOGGER.log(Level.SEVERE,"Error while sending LOGIN",e);
		}
		
		try{ 
			for(String v:c.sendCommand(ImapLine.getNextTag()+" CAPABILITY")) { 
				LOGGER.log(Level.FINEST,"IMAP<- C: "+v); 
			} 
		} catch(Exception e) {
			LOGGER.log(Level.SEVERE,"Error while sending CAPABILITY",e);
		}
			
		try{ 
			for(String v:c.sendCommand(ImapLine.getNextTag()+" LOGOUT")) { 
				LOGGER.log(Level.FINEST,"IMAP<- C: "+v); 
			} 
		} catch(TimeoutException e) {
			LOGGER.log(Level.SEVERE,"Error while sending LOGOUT",e);
		}

		LOGGER.log(Level.INFO,"ended ending commands");
		LOGGER.log(Level.INFO,"shutting down server");
		s.shutdown();
		LOGGER.log(Level.INFO,"server shutdown completed");
		//c.shutdown();
    }
	 
}
