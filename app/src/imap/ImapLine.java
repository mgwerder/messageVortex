package net.gwerder.java.mailvortex.imap;

import java.util.logging.Logger;
import java.util.logging.Level;  
  
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/***
/**
 * A Imap conformant parser/scanner.
 *
 * @author Martin Gwerder
 * @fix.me Limit strings and literals to a certain length (otherwise it is litterally unlimited) 
 * @known.bugs Code will fail i Line.length()>Maxint or out of memory
 ***/
public class ImapLine {
    
    /* specifies the context range which is given when an exception arises during scanning */
    private static final int MAX_CONTEXT=30;
    
    /* These are character subsets specified in RFC3501 */
    private static final String ABNF_SP = " ";
    private static final String ABNF_CTL = charlistBuilder(0,31);
    private static final String ABNF_LIST_WILDCARDS = "*%";
    private static final String ABNF_QUOTED_SPECIALS = "\"\\";
    private static final String ABNF_RESP_SPECIALS = "[";
    private static final String ABNF_ATOM_SPECIALS = "(){"+ABNF_SP+ABNF_CTL+ABNF_LIST_WILDCARDS+ABNF_QUOTED_SPECIALS+ABNF_RESP_SPECIALS;
    private static final String ABNF_ATOM_CHAR = charlistDifferencer(charlistBuilder(1,127),ABNF_ATOM_SPECIALS);
    private static final String ABNF_TEXT_CHAR = charlistDifferencer(charlistBuilder(1,127),"\r\n");
    private static final String ABNF_QUOTED_CHAR = charlistDifferencer(ABNF_TEXT_CHAR,ABNF_QUOTED_SPECIALS);
    private static final String ABNF_TAG=charlistDifferencer(ABNF_ATOM_CHAR,"+");

    /* a Logger  for logging purposes */
    private final Logger LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());

    /* this holds the past context for the case that an exception is risen */
    private String context = "";
    
    private static int    tagEnumerator=0;
    private static final Object tagEnumeratorLock=new Object();
    
    /* storage for the connection which created the line */
    private ImapConnection con;
    
    /* The holder for the parsed command/status of a line */
    private String commandToken=null;
    
    /* The holder for the parsed tag of a line */
    private String tagToken=null;

    /* This input stream is the source of subsequent characters */
    private InputStream input=null;
    
    /* this is the buffer of read but unprocessed characters */
    private String buffer="";

    /***
     * Trivial constructor omiting a stream.
     *
     * This constructor is mainly meant for testing purposes
     *
     * @param con   The ImapConnection object which generated the Command line
     * @param line  The String which has already been read (as Read ahead)
     ***/
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
        this.buffer=line;
         
        // check if nothing at all (no even an empty line) has been passed
        if(this.buffer==null && this.input==null) {
            throw new ImapException(this,"null String passed");
        }

        // make sure that we have a valid InputStream
        if(this.input==null) {
            this.input=new ByteArrayInputStream("".getBytes());
        }
        
        // make sure that a line is never null when reaching the parsing section
        if(buffer==null) {
            buffer="";
        }
        
        if(snoopBytes(1)==null || "\r\n".equals(snoopBytes(2)) || "".equals(snoopBytes(1))) {
            skipUntilCRLF();
            throw new ImapBlankLineException(this);
        }
        
        // getting a tag
        tagToken=getATag();
        
        if(tagToken==null) {
            skipUntilCRLF();
            throw new ImapException(this, "error getting tag");
        }
        
        int i=skipSP(1);
        if(i!=1) {
            throw new ImapException(this,"error skipping to command (line=\""+line+"\"; tag="+tagToken+"; buffer="+buffer+"; skipped="+i+")");
        }

        // get command
        commandToken = getATag();
        if(tagToken==null) {
            skipUntilCRLF();
            throw new ImapException(this, "error getting command");
        }
        
        if("BAD".equalsIgnoreCase(commandToken) || "OK".equalsIgnoreCase(commandToken) || "*".equals(tagToken)) {
            LOGGER.log(Level.INFO,"Parsing command "+tagToken+" "+commandToken);
        }    
        
        skipSP(-1);
    }
    
    /***
     * Builds a set of chracters ranging from the ASCII code of start until the ASCII code of end.
     *
     * This helper is mainly used to build ABNF strings.
     *
     * @param start The first ASCII code to be used
     * @param end   The last ASCII code to be used
     ***/
    public static String charlistBuilder(int start, int end) {
        // reject chain building if start is not within 0..255
        if(start<0 || start>255) {
            return null;
        }

        // reject chain building if end is not within 0..255
        if(end<0 || end>255) { 
            return null;
        }
        
        // reject chain building if start>end
        if(end<start) {
            return null;
        }    

        // build the string
        String ret="";
        for(int i=start;i<=end;i++) {
            ret+=(char)i;
        }
        return ret;
    }    

    /***
     * Removes a given set of characters from a superset.
     *
     * @param superset    The set where character should be removed from
     * @param subset      The set of characters to be removed
     ***/
    public static String charlistDifferencer(String superset,String subset) {
        String ret=superset;
        for(int i=0;i<subset.length();i++) {
            char c=subset.charAt(i);
            ret=ret.replace(""+c,"");
        }
        return ret;
    }    

    /***
     * Getter for the Imap connection in Control of this command.
     *
     * @returns ImapConnection storing the context of this command
     ***/
    public ImapConnection getConnection() {
        return con;
    }
    
    public String getCommand() {
        return commandToken;
    }
    
    public String getTag() {
        return tagToken;
    }
    
    private boolean readBuffer(long num) {
        // make sure that we have sufficient bytes in the buffer
        boolean ended=false;
        try{
            while(buffer.length()<num && !ended) {
                int i=input.read();
                if(i>=0) {
                    buffer+=(char)i;
                } else {
                    ended=true;
                }   
            }
        } catch(IOException ioe) {
            ended=true;
        }
        return ended;
    }
    
    public String snoopBytes(long num) {
        if(num<=0) {
            return null;
        }

        // build buffer
        readBuffer(num);
        
        if(buffer.length()==0) {
            return null;
        }
        
        // if the string is too short -> return it
        if(buffer.length()<num) {
            return buffer;
        }
        
        return buffer.substring(0,(int)num);
    }
    
    private void addContext(String chunk) {
        context+=chunk;
        if(context.length()>MAX_CONTEXT) {
            context=context.substring(context.length()-MAX_CONTEXT,context.length());
        }
    }
    
    public String getContext() {
        return context+"^^^"+buffer;
    }
    
    public String skipBytes(long num) {
        return skipBytes(num,true);
    }
    
    public String skipBytes(long num,boolean modContext) {
        // make sure that we have sufficient bytes in the buffer
        readBuffer(num);
                
        // if the string is too short -> return it
        if(buffer.length()<num) {
            buffer="";
            if(modContext) {
                addContext(buffer);
            }
            return buffer;
        }
        
        // prepare return result
        String ret=snoopBytes(num);
        
        // update context
        if(modContext) {
            addContext(ret);
        }
        
        // cut the buffer
        buffer=buffer.substring((int)num,buffer.length());
        
        return ret;
    }
    
    public int skipSP(int num) {
        // count number of spaces found
        int count=0;
        
        // init countdown counter
        int countdown=num;
        
        // loop thru skipper
        while(snoopBytes(1)!=null && ABNF_SP.contains(snoopBytes(1)) && (countdown!=0)) {
            skipBytes(1);
            count++;
            countdown--;
        }
        
        // return count of spaces skipped
        return count;
    }

    public boolean skipCRLF() {
        if(snoopBytes(2)!=null && "\r\n".equals(snoopBytes(2))) {
            skipBytes(2);
            return true;
        }    
        return false;
    }
    
    public boolean skipUntilCRLF() {
        while(snoopBytes(2)!=null && !"\r\n".equals(snoopBytes(2))) {
            skipBytes(1,false);
        }
        
        if(snoopBytes(2)!=null && "\r\n".equals(snoopBytes(2))) {
            skipBytes(2,false);
            return true;
        }    
        return false;
    }
    
    public String getString() {
        String start=snoopBytes(1);
        String ret=null;
        
        if("{".equals(start)) {
            
            // get a length prefixed sting
            
            //skip curly brace
            skipBytes(1);
            
            // get number
            
            long num=0;
            while(snoopBytes(1)!=null && "0123456789".contains(snoopBytes(1)) && num<4294967295L) {
                num=num*10+(int)(skipBytes(1).charAt(0))-(int)("0".charAt(0));
            }
            if(num<0 || num>4294967295L) {
                return null;
            }
            
            // skip rcurly brace
            ret=skipBytes(1);
            if(ret==null || !"}".equals(ret)) {
                return null;
            }
            
            // skip crlf
            skipCRLF();
            
            // get String
            ret=skipBytes(num);
            
            if(ret==null || ret.length()<num) {
                return null;
            }    
            
        } else if("\"".equals(start)) {
            
            // get a quoted string
            skipBytes(1);
            ret="";
            while(snoopBytes(1)!=null && (ABNF_QUOTED_CHAR.contains(snoopBytes(1)) || ("\\".contains(snoopBytes(1)) && snoopBytes(2).length()==2 && ABNF_QUOTED_SPECIALS.contains(snoopBytes(2).substring(1,2))))) {
                if("\\".contains(snoopBytes(1))) {
                    ret+=skipBytes(2).substring(1,2);
                } else {
                    ret+=skipBytes(1);
                }
            }
            if(snoopBytes(1)==null || !"\"".equals(skipBytes(1))) {
                return null;
            }    
        }    
        
        return ret;
    }
    
    public String getAString() {
        
        String start=snoopBytes(1);
        String ret=null;
        
        if("{".equals(start) || "\"".equals(start)) {

            // get a dquoted or literal (length prefixed) String
            ret=getString();
            
        } else {

            // get a sequence of atom chars
            while(snoopBytes(1)!=null && ABNF_ATOM_CHAR.contains(snoopBytes(1))) {
                if(ret==null) {
                    ret="";
                }
                ret+=skipBytes(1);
            }
        }
        
        return ret;
    }
    
    public String getATag() {
        String ret=null;
        
        // get a sequence of atom chars
        while(snoopBytes(1)!=null && ABNF_TAG.contains(snoopBytes(1))) {
            if(ret==null) {
                ret="";
            }
            ret+=skipBytes(1);
        }
        
        if("".equals(ret)){
            // empty tags are not alowed. At least one char is required
            return null;
        }    
        
        return ret;
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