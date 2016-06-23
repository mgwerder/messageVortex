package net.gwerder.java.mailvortex.imap;

public class ImapCommandCapability extends ImapCommand {

    public void init() {
        ImapCommand.registerCommand(this);
    }
    
    private String addCapability(String fullCap,String cap) throws ImapException {
        String t=fullCap;
        if(cap.indexOf('=')==-1) {
            t+=" "+cap;
        } else {
            String[] v=cap.split("=");
            if(v.length!=2) {
                throw new ImapException(null,"got illegal capability \""+cap+"\" from command");
            }
            if(t.indexOf(v[0]+"=")>-1) {
                t=t.replace(v[0]+"=",v[0]+"="+v[1]+",");
            } else {
                t+=" "+cap;
            }
        }
        return t;
    }
    
    
    public String[] processCommand(ImapLine line) throws ImapException {

        // skip space
        // WRNING this is "non-strict"
        line.skipSP(-1);
        
        // skip lineend
        if(!line.skipCRLF()) {
            throw new ImapException(line,"error parsing command");
        }

        ImapCommand[] arr=ImapCommand.getCommands();
        String cap="";
        
        // looping thru commands
        for(ImapCommand ic:arr) {

            String[] arr2=ic.getCapabilities();
            if(arr2!=null) {
                for(String a2:arr2) {
                    cap=addCapability(cap,a2);
                }
            }
        }
        return new String[] {"* CAPABILITY IMAP4rev1"+cap+"\r\n",line.getTag()+" OK\r\n" };
    }
    
    public String[] getCommandIdentifier() {
        return new String[] {"CAPABILITY"};
    }
    
    public String[] getCapabilities() {
        return new String[] {};
    }
    
}    
