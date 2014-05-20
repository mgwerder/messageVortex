package net.gwerder.java.mailvortex.imap;

public class ImapLine {

	private static int    identifierEnumertor=0;
	private static final Object identifierEnumertorLock=new Object();
	private String identifier=null;

	public ImapLine(String line) {
		// FIXME implementation missing
	}

	public String getIdentifier() {
		return identifier;
	}
	
	public static String getNextIdentifier() {
		return getNextIdentifier("A");
	}
		
	public static String getNextIdentifier(String prefix) {
		String ret;
		synchronized(identifierEnumertorLock) {
			identifierEnumertor++;
			ret=prefix+String.format("%06d", identifierEnumertor);
		}
		return ret;
	}
}