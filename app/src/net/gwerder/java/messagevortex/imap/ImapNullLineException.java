package net.gwerder.java.messagevortex.imap;

public class ImapNullLineException extends ImapException {

    private static final long serialVersionUID = 44L;

    public ImapNullLineException(ImapLine line) {
        super(line,"Received blank line (null)");
    }
}