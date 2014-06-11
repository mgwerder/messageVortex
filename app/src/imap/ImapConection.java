package net.gwerder.java.mailvortex.imap;
 
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executors;
import java.util.Set;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLContext;
import java.net.Socket;


class ImapConnection extends StoppableThread implements Comparable<ImapConnection> {

	static public final int CONNECTION_NOT_AUTHENTICATED = 1;
	static public final int CONNECTION_AUTHENTICATED     = 2;
	static public final int CONNECTION_SELECTED          = 3;
	
	static private int defaultTimeout = 60;
	
	private int timeout=defaultTimeout;
	
	private Socket plainSocket=null;
	private SSLSocket sslSocket=null;
	private Socket currentSocket=null;
	private SSLContext context;
	private int status=CONNECTION_NOT_AUTHENTICATED;
	private Set<String> suppCiphers;
	private boolean encrypted=false;
	private ImapAuthenticationProxy authProxy = null;
	private InputStream input=null;
	private OutputStream output=null;
	
	public ImapConnection(Socket sock,SSLContext context,Set<String> suppCiphers, boolean encrypted) 
	{
		this.plainSocket=sock;	
		this.context=context;
		this.suppCiphers=suppCiphers;
		this.encrypted=encrypted;
		updateSocket();
		Executors.newSingleThreadExecutor().execute(this);
	}
	
	public ImapAuthenticationProxy setAuth(ImapAuthenticationProxy authProxy) {
		ImapAuthenticationProxy oldProxyAuth=getAuth();
		this.authProxy=authProxy;
		this.authProxy.setImapConnection(this);
		return oldProxyAuth;
	}
	
	public ImapAuthenticationProxy getAuth() {
		return this.authProxy;
	}
	
	public int setTimeout(int timeout) {
		int ot=this.timeout;
		this.timeout=timeout;
		return ot;
	}
	
	public int getTimeout() { return this.timeout; }
	
	public static int setDefaultTimeout(int timeout) {
		int ot=defaultTimeout;
		defaultTimeout=timeout;
		return ot;
	}
	
	public static int getDefaultTimeout() { return defaultTimeout; }
	
	public int setState(int status) {
		if(status>3 || status<1) return -status;	
		int old=status;
		this.status=status;
		return old;
	}
	
	private void updateSocket() {
		if(sslSocket!=null) {
			currentSocket=sslSocket;
		} else {
			currentSocket=plainSocket;
		}
		if(plainSocket!=null) {
			try{
				input=currentSocket.getInputStream();
				output=currentSocket.getOutputStream();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public int compareTo(ImapConnection i) {
		if(this==i) {
			return 0;
		}else {
			return -1;
		}	
	}
	
	public int shutdown() {
		// FIXME good implementation missing
		shutdown=true;
		try{
			this.join();
		} catch(InterruptedException e) {}
		return 0;
	}
	
	private boolean startTLS() throws IOException {
		this.sslSocket = (SSLSocket) context.getSocketFactory().createSocket(plainSocket,plainSocket.getInetAddress().getHostAddress(),plainSocket.getPort(),false);
		String[] arr = suppCiphers.toArray(new String[0]);
		for(int i=0;i<arr.length;i++) {
			System.out.println("-- "+arr[i]);
		}
		this.sslSocket.setUseClientMode(false);
		this.sslSocket.setEnabledCipherSuites(arr);
		this.sslSocket.startHandshake();
		return false;
	}
	
	public boolean isTLS() {
		return sslSocket!=null;
	}
	
	private String[] processCommand(String command) {
		// Extract first word (command) and fetch respective command object
		ImapLine il=null;
		try{
			il=new ImapLine(this,command);
		} catch(ImapBlankLineException ie) {
			// just ignore blank lines
			return new String[0];
		} catch(ImapException ie) {
			return new String[] {ie.getTag()+" BAD "+ie.toString()};
		}
		
		// FIXME implementation missing
		return new String[0];
	}
	
	public void run() {
		try{
			if(encrypted) startTLS();
			if(sslSocket!=null) sslSocket.close();
			while(!shutdown) {
				// FIXME wait for commands and send reply
				// FIXME timeout
				Thread.sleep(10);// FIXME remove me after implementation
			}
			plainSocket.close();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(InterruptedException e) {
			e.printStackTrace();
		} finally {	
			try{
				if(sslSocket!=null) sslSocket.close();
				plainSocket.close();
			}catch(Exception e2) {}
		}	
	}
	
}
 
