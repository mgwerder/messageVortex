package net.gwerder.java.mailvortex.imap;

public class ImapCommandCapability extends ImapCommand {

    static void init() {
        ImapCommand.registerCommand(new ImapCommandCapability());
    }
    
    public String[] processCommand(ImapLine line) {
        ImapCommand[] arr=ImapCommand.getCommands();
        String cap="";
        for(int i=0;i<arr.length;i++) {
            // FIXME process =-entries
            String[] arr2=arr[i].getCapabilities();
            if(arr2!=null) {
                for(int j=0;j<arr2.length;j++) {
                    cap+=" "+arr2[j];
                }
            }
        }
        return new String[] {"* CAPABILITY IMAP4rev1"+cap,line.getTag()+" OK" };
    }
    
    public String[] getCommandIdentifier() {
        return new String[] {"CAPABILITY"};
    }
    
}    
