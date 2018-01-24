package net.gwerder.java.messagevortex.transport;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Martin on 24.01.2018.
 */
public class ServerAuthenticator {

    private Map<String,Credentials> creds=new HashMap<>();

    public void addCredentials(Credentials cred) {
        creds.put(cred.getUsername().toLowerCase(),cred);
    }

    public Credentials removeCredentials(String user) {
        return creds.remove(user.toLowerCase());
    }

    public Credentials getCredentials(String user) {
        return creds.get(user.toLowerCase());
    }
}
