package net.gwerder.java.mailvortex.imap;

public class ImapBlankLineException extends ImapException {

    private static final long serialVersionUID = 43L;

    public ImapBlankLineException(ImapLine line) {
        super(line,"Received blank line");
    }
}