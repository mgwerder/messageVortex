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
        ImapServer s=new ImapServer(143,encrypted);
        ImapClient c=new ImapClient("localhost",143,encrypted);
		try{ for(String v:c.sendCommand(ImapLine.getNextTag()+" CAPABILITIES")) { System.out.println("C: "+v); } } catch(Exception e) {e.printStackTrace();}
		try{ for(String v:c.sendCommand(ImapLine.getNextTag()+" LOGIN user password")) { System.out.println("C: "+v); } } catch(TimeoutException e) {e.printStackTrace();}
		try{ for(String v:c.sendCommand(ImapLine.getNextTag()+" CAPABILITIES")) { System.out.println("C: "+v); } } catch(TimeoutException e) {e.printStackTrace();}
		try{ for(String v:c.sendCommand(ImapLine.getNextTag()+" LOGOUT")) { System.out.println("C: "+v); } } catch(TimeoutException e) {e.printStackTrace();}
		try{ for(String v:c.sendCommand(ImapLine.getNextTag()+" CAPABILITIES")) { System.out.println("C: "+v); } } catch(TimeoutException e) {e.printStackTrace();}
		try{ for(String v:c.sendCommand(ImapLine.getNextTag()+" BYE")) { System.out.println("C: "+v); } } catch(TimeoutException e) {e.printStackTrace();}
    }
	 
}
