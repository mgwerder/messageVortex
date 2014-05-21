package net.gwerder.java.mailvortex.imap;
 
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.Set;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLContext;
import java.net.Socket;


class ImapConnection extends StoppableThread implements Comparable<ImapConnection> {

	static public final int CONNECTION_NOT_AUTHENTICATED = 1;
	static public final int CONNECTION_AUTHENTICATED     = 2;
	static public final int CONNECTION_SELECTED          = 3;
	
	private Socket plainSocket;
	private SSLSocket sslSocket=null;
	private Socket currentSocket;
	private SSLContext context;
	private int status=CONNECTION_NOT_AUTHENTICATED;
	private Set<String> suppCiphers;
	private boolean encrypted;
	
	public ImapConnection(Socket sock,SSLContext context,Set<String> suppCiphers, boolean encrypted) 
	{
		this.plainSocket=sock;	
		this.context=context;
		this.suppCiphers=suppCiphers;
		this.encrypted=encrypted;
		updateSocket();
		Executors.newSingleThreadExecutor().execute(this);
	}
	
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
	}
	
	public int compareTo(ImapConnection i) {
		if(this==i) {
			return 0;
		}else {
			return -1;
		}	
	}
	
	public int shutdown() {
		// FIXME implementation missing
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
	
	public void run() {
		try{
			if(encrypted) startTLS();
			if(sslSocket!=null) sslSocket.close();
			while(!shutdown) {
				// FIXME wait for commands and send reply
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
 
