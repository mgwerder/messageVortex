package net.gwerder.java.mailvortex.imap;
 
 
/**
 * @author Martin Gwerder
 * FIXME Trust and keystoreimplementation broken
 */
public class ImapSSLSocket {

	public static void registerCommand(ImapCommand command) {
		// FIXME
	}

	/**
     * Selftest function for IMAP server.
     *
     * @param args      ignored commandline arguments
     */
    public static void main(String[] args) throws Exception {
		boolean encrypted=false;
        ImapServer s=new ImapServer(143,encrypted);
        new ImapClient("localhost",143,encrypted);
    }
	 
}
