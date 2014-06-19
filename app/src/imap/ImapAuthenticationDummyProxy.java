package net.gwerder.java.mailvortex.imap;

import java.util.HashMap;

public class ImapAuthenticationDummyProxy extends ImapAuthenticationProxy{

    private final HashMap<String,String> users=new HashMap<String,String>();
    
    public void addUser(String username,String password) {
        users.put(username.toLowerCase(),password);
    }

    public boolean login(String username,String password) {
        // Always require a username or password
        if(username==null || password==null) return false;
        
        // check if user exists
        if(users.get(username.toLowerCase())==null) return false;
        
        // check if password is correct
        return users.get(username.toLowerCase()).equals(password);
    }


}
