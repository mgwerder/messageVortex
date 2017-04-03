package net.gwerder.java.messagevortex.imap;

public abstract class ImapAuthenticationProxy {

    private ImapConnection conn=null;

    public abstract boolean login(String username,String password);
    
    public ImapConnection setImapConnection(ImapConnection conn) {
        ImapConnection oc=this.conn;
        this.conn=conn;
        return oc;
    }

    /***
     * Get the ImapConnection object which belongs to this proxy
     *
     * @return A Connection object which is connected to this proxy
     ***/
    public ImapConnection getImapConnection() { 
        return this.conn; 
    }
    
}
