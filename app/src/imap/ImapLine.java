package net.gwerder.java.mailvortex.imap;

import java.io.InputStream;

public class ImapLine {

	private static int    tagEnumerator=0;
	private static final Object tagEnumeratorLock=new Object();
	private ImapConnection con;
	private String commandToken=null;
	private String tagToken=null;
	private InputStream input=null;

	public ImapLine(ImapConnection con,String line) throws ImapException {this(con,line,null);}
	
	public ImapLine(ImapConnection con,String line,InputStream input) throws ImapException {
		this.con=con;
		this.input=input;
		
		if(line==null) throw new ImapException(this,"null line passed");
		
		if(line.length()==0) throw new ImapBlankLineException(this);
		
		// get first two tokens
		String[] tokens=line.split("\\p{Space}+");
		tagToken=line;
		if(tokens.length<2) {
			throw new ImapException(this,"Command token not found");
		}
		tagToken=tokens[0];
		commandToken=tokens[1];
			
		// FIXME implementation missing
	}

	public ImapConnection getConnection() {
		return con;
	}
	
	public String getCommand() {
		return commandToken;
	}
	
	public String getTag() {
		return tagToken;
	}
	
	public static String getNextTag() {
		return getNextTag("A");
	}
		
	public static String getNextTag(String prefix) {
		String ret;
		synchronized(tagEnumeratorLock) {
			tagEnumerator++;
			ret=prefix+String.format("%d", tagEnumerator);
		}
		return ret;
	}
}