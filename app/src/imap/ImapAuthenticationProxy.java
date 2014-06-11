package net.gwerder.java.mailvortex.imap;

public abstract class ImapAuthenticationProxy {

	private ImapConnection conn=null;

	public abstract boolean login(String username,String password);
	
	public ImapConnection setImapConnection(ImapConnection conn) {
		ImapConnection oc=this.conn;
		this.conn=conn;
		return oc;
	}

	public ImapConnection getImapConnection() { return this.conn; }
	
}
