package net.gwerder.java.mailvortex.imap;

public class ImapException extends Exception {

    private ImapLine line;

    public ImapException(ImapLine line,String reason) {
        super(reason);
        this.line=line;
    }
    
    public String getTag() {
        return line.getTag();
    }
}