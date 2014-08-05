package net.gwerder.java.mailvortex.imap;

public class ImapCommandCapability extends ImapCommand {

    static void init() {
        ImapCommand.registerCommand(new ImapCommandCapability());
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
        for(int i=0;i<arr.length;i++) {

            String[] arr2=arr[i].getCapabilities();
            if(arr2!=null) {
                for(int j=0;j<arr2.length;j++) {
                    if(arr2[j].indexOf('=')==-1) {
                        cap+=" "+arr2[j];
                    } else {
                        String[] v=arr2[j].split("=");
                        if(v.length!=2) {
                            throw new ImapException(null,"got illegal capability \""+arr2[j]+"\" from "+arr[i]);
                        }
                        if(cap.indexOf(v[0]+"=")>-1) {
                            cap=cap.replace(v[0]+"=",v[0]+"="+v[1]+",");
                        } else {
                            cap+=" "+arr2[j];
                        }
                    }
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
