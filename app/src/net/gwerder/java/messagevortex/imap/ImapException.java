package net.gwerder.java.messagevortex.imap;

public class ImapException extends Exception {

    private static final long serialVersionUID = 42L;
    private final ImapLine line;

    public ImapException(ImapLine line,String reason) {
        super(line==null?reason:reason+" at \""+line.getContext()+"\"");
        this.line=line;
    }
    
    public String getTag() {
        return line.getTag();
    }
    
    
}