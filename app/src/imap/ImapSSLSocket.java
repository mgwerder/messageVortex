package net.gwerder.java.mailvortex.imap;

import java.util.concurrent.TimeoutException;
 
/**
 * @author Martin Gwerder
 */
public class ImapSSLSocket {

	/**
     * Selftest function for IMAP server.
     *
     * @param args      ignored commandline arguments
     ***/
    public static void main(String[] args) throws Exception {
		boolean encrypted=false;
        ImapServer s=new ImapServer(0,encrypted);
        ImapClient c=new ImapClient("localhost",s.getPort(),encrypted);
		System.out.println("## Sending commands");
		try{ for(String v:c.sendCommand(ImapLine.getNextTag()+" CAPABILITY")) { System.out.println("aIMAP<- C: "+v); } } catch(Exception e) {e.printStackTrace();}
		try{ for(String v:c.sendCommand(ImapLine.getNextTag()+" LOGIN user password")) { System.out.println("aIMAP<- C: "+v); } } catch(TimeoutException e) {e.printStackTrace();}
		try{ for(String v:c.sendCommand(ImapLine.getNextTag()+" CAPABILITY")) { System.out.println("aIMAP<- C: "+v); } } catch(Exception e) {e.printStackTrace();}
		try{ for(String v:c.sendCommand(ImapLine.getNextTag()+" LOGOUT")) { System.out.println("aIMAP<- C: "+v); } } catch(TimeoutException e) {e.printStackTrace();}
		System.out.println("## ended ending commands");
		System.out.println("## shutting down server");
		s.shutdown();
		System.out.println("## server shutdown completed");
		//c.shutdown();
    }
	 
}
