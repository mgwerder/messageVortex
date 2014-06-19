package net.gwerder.java.mailvortex.imap;

import java.util.logging.Logger;
import java.util.logging.Level;  
  
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;


public class ImapLine {

    private static final Logger LOGGER;
    static {
        LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    private static int    tagEnumerator=0;
    private static final Object tagEnumeratorLock=new Object();
    private ImapConnection con;
    private String commandToken=null;
    private String tagToken=null;
    private InputStream input=null;

    public ImapLine(ImapConnection con,String line) throws ImapException {
        this(con,line,null);
    }
    
    public ImapLine(ImapConnection con,String line,InputStream input) throws ImapException {
        this.con=con;
        this.input=input;
         
        // make sure that we have a valid InputStream
        if(this.input==null) {
            this.input=new ByteArrayInputStream("".getBytes());
        }
        
        // get first two tokens
        // Trivial implementation (just wait fo a newline)
        int b=0;
        while(!line.endsWith("\r\n") && b!=-1) {
            try{
                b=this.input.read();
                if(b>=0) line+=(char)b;
            } catch(IOException ioe) {
                LOGGER.log(Level.WARNING, "IOException rised while reading line (Ungraceful connection close by client?");
            }    
        }   
        
        if(line==null) throw new ImapException(this,"null String passed");
        if(line.length()==0) throw new ImapBlankLineException(this);
        
        // parse token and command
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