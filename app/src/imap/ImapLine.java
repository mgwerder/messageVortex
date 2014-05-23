package net.gwerder.java.mailvortex.imap;

public class ImapLine {

	private static int    identifierEnumerator=0;
	private static final Object identifierEnumeratorLock=new Object();
	private String identifier=null;
	private ImapConnection con;

	public ImapLine(ImapConnection con,String line) {
		this.con=con;
		// FIXME implementation missing
	}

	public ImapConnection getConnection() {
		return con;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public static String getNextIdentifier() {
		return getNextIdentifier("A");
	}
		
	public static String getNextIdentifier(String prefix) {
		String ret;
		synchronized(identifierEnumeratorLock) {
			identifierEnumerator++;
			ret=prefix+String.format("%06d", identifierEnumerator);
		}
		return ret;
	}
}