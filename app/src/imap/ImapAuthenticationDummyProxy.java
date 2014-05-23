package net.gwerder.java.mailvortex.test;

import java.util.HashMap;

public class ImapAuthenticationDummyProxy extends ImapAuthenticationProxy{

	private final HashMap<String,String> users=new HashMap<String,String>();
	
	public void addUser(String username,String password) {
		users.put(username,password);
	}

	public boolean login(String username,String password) {
		if(users.get(username)==null) return false;
		if(users.get(username).equals(password)) return true;
		return false;
	}


}
