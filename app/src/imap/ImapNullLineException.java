package net.gwerder.java.mailvortex.imap;

public class ImapNullLineException extends ImapException {

    public ImapNullLineException(ImapLine line) {
        super(line,"Received blank line (null)");
    }
}