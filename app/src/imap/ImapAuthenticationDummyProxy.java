package net.gwerder.java.mailvortex.imap;

import java.util.HashMap;
import java.util.Map;

public class ImapAuthenticationDummyProxy extends ImapAuthenticationProxy{

    private final Map<String,String> USERS=new HashMap<String,String>();
    
    public void addUser(String username,String password) {
        USERS.put(username.toLowerCase(),password);
    }

    public boolean login(String username,String password) {
        // Always require a username or password
        if(username==null || password==null) {
            return false;
        }
        
        // check if user exists
        if(USERS.get(username.toLowerCase())==null) {
            return false;
        }
        
        // check if password is correct
        return USERS.get(username.toLowerCase()).equals(password);
    }


}
