package net.gwerder.java.mailvortex.imap;

public class ImapBlankLineException extends ImapException {

	public ImapBlankLineException(ImapLine line) {
		super(line,"Received blank line");
	}
}