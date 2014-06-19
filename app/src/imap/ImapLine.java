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
    
    /***
     * Creates an imap line object with a parser for a command.
     *
     * A passed input stream is appended to line. Reading takes place according to the ABNF-Rules defined in the respective RFC
     *
     * @param con   The ImapConnection object which generated the Command line
     * @param line  The String which has already been read (as Read ahead)
     * @param input The Stream offering more data to read if required
     *
     * @fix.me should be a ABNF implementation
     * @fix.me extract reading from constructor
     ***/
    public ImapLine(ImapConnection con,String line,InputStream input) throws ImapException {
        this.con=con;
        this.input=input;
         
        // check if nothing at all (no even an empty line) has been passed
        if(line==null && this.input==null) {
            throw new ImapException(this,"null String passed");
        }

        // make sure that we have a valid InputStream
        if(this.input==null) {
            this.input=new ByteArrayInputStream("".getBytes());
        }
        
        // make sure that a line is never null when reaching the parsing section
        if(line==null) {
            line="";
        }
        
        // get first two tokens
        // Trivial implementation (just wait fo a newline)
        int b=0;
        while(!line.endsWith("\r\n") && b!=-1) {
            try{
                b=this.input.read();
                if(b>=0) {
                    line+=(char)b;
                }
            } catch(IOException ioe) {
                LOGGER.log(Level.WARNING, "IOException rised while reading line (Ungraceful connection close by client?");
            }    
        }   
        
        if(line.length()==0) {
            throw new ImapBlankLineException(this);
        }
        
        // parse token and command
        String[] tokens=line.split("\\p{Space}+");
        tagToken=line;
        if(tokens.length<2) {
            throw new ImapException(this,"Command token not found");
        }
        tagToken=tokens[0];
        commandToken=tokens[1];
            
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