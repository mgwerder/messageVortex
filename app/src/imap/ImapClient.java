package net.gwerder.java.mailvortex.imap;
 
import java.util.List;
import java.util.ArrayList;
import java.net.Socket;
import javax.net.ssl.SSLSocket;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.FileInputStream;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ImapClient implements Runnable {

	private static final int DEFAULT_TIMEOUT=10000;

	private String targetHost="localhost";
	private Object sync=new Object();
	private Object notifyThread=new Object();
	private int targetPort = 143;
	private boolean encrypted;
	private boolean shutdown=false;
	private String currentCommand=null;
	private String[] currentCommandReply=null;
	private boolean currentCommandCompleted=false;
	private final TrustManager[] trustAllCerts = new TrustManager[] {  new X509TrustManager() {     
			public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null;	} 
			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {} 
			public void checkServerTrusted( java.security.cert.X509Certificate[] certs, String authType) {}
	}}; 
	Socket socket=null;
	private Thread runner=null;	
	public ImapClient(String targetHost,int targetPort,boolean encrypted) {
		this.targetHost=targetHost;
		this.targetPort=targetPort;
		this.encrypted=encrypted;
		runner=new Thread(this,"ImapClient command processor");
		runner.setDaemon(true);
		runner.start();
	}

	private Socket startTLS(Socket sock) throws IOException,java.security.NoSuchAlgorithmException,java.security.KeyManagementException,java.security.KeyStoreException,java.security.cert.CertificateException {
		java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		trustStore.load(new FileInputStream("keystore.jks"), "changeme".toCharArray());
		TrustManagerFactory trustFactory =  TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());    
		trustFactory.init(trustStore);
		TrustManager[] trustManagers = trustFactory.getTrustManagers();
		SSLContext trustContext = SSLContext.getInstance("SSL");
		trustContext.init(null, trustManagers, null);
		SSLContext.setDefault(trustContext);
		SSLSocket sslSocket = (SSLSocket)(((SSLSocketFactory)(trustContext.getSocketFactory().getDefault())).createSocket(sock,sock.getInetAddress().getHostAddress(),sock.getPort(), false));    
		sslSocket.setUseClientMode(true);    
		System.out.println("## Starting client side SSL");
		sslSocket.startHandshake();
		System.out.println("CLientTLS Started");
		//String[] arr=((SSLSocket)(sslSocket)).getEnabledCipherSuites();
		//for(int i=0;i<arr.length;i++) System.out.println(" supported by client: "+arr[i]);
		return sslSocket;
	}
	
	public String[] sendCommand(String command) throws TimeoutException { return sendCommand(command,DEFAULT_TIMEOUT); }
	
	public String[] sendCommand(String command,int millisTimeout) throws TimeoutException {
		synchronized(sync) {
			currentCommand=command;
			long start = System.currentTimeMillis();
			currentCommandCompleted=false;
			synchronized(notifyThread) {notifyThread.notify(); }
			while(!currentCommandCompleted && System.currentTimeMillis()<start+millisTimeout) {
				try{sync.wait(100);} catch(InterruptedException e) {};
			}
			System.out.println("## wakeup succeeded");
			if(!currentCommandCompleted && System.currentTimeMillis()>start+millisTimeout) throw new TimeoutException("Timeout reached while sending \""+command+"\"");
		}
		currentCommand=null;
		return currentCommandReply;
	}
	
	public void shutdown() {
		shutdown=true;
		try {
			try{
				synchronized(notifyThread) {
					notifyThread.notify(); 
				};
				if(socket!=null) {
					socket.getOutputStream().close();
					socket.getInputStream().close();
					socket.close();
				}
			} catch(IOException ioe) {;}	
			runner.join();
		} catch(InterruptedException ie) {
			; // Intentionally left blank
		}	
	}	

	public void run() {

		try {
			socket = SocketFactory.getDefault().createSocket(targetHost,targetPort);
			if(encrypted) socket=startTLS(socket);
			while(!shutdown && !socket.isClosed() && !socket.isInputShutdown() && !socket.isOutputShutdown()) {
				try{
					synchronized(notifyThread) {try{notifyThread.wait(100);} catch(InterruptedException e) {} }
					if(currentCommand!=null && !currentCommand.equals("")) {
						System.out.println("IMAP-> C: "+currentCommand);
						socket.getOutputStream().write((currentCommand+"\r\n").getBytes());
						socket.getOutputStream().flush();
				
						String tag=null;
						ImapLine il=null;
						try{
							il=new ImapLine(null,currentCommand);
							tag=il.getTag();
						} catch(ImapException ie) {}
						String reply="";
						String lastReply="";
						List<String> l=new ArrayList<String>();
						int i=0;
						do{
							i=socket.getInputStream().read();
							if(i>=0) reply+=(char)i;
							if(reply.endsWith("\r\n")) {
								l.add(reply);
								System.out.println("IMAP<- C: "+reply.substring(0,reply.length()-2));
								currentCommandReply=l.toArray(new String[0]);
								lastReply=reply.substring(0,reply.length()-2);
								reply="";
							}
						} while(!lastReply.matches(tag+"\\s+BAD.*") && !lastReply.matches(tag+"\\s+OK.*") && i>=0);
						currentCommandCompleted=lastReply.matches(tag+"\\s+BAD.*") || lastReply.matches(tag+"\\s+OK.*");
						currentCommand=null;
						if(il!=null && il.getCommand().toLowerCase().equals("logout") && lastReply.matches(tag+"\\s+OK.*")) {
							// Terminate connection on successful logout
							shutdown=true;
						}	
						lastReply="";
						synchronized(sync) {sync.notify(); }

						System.out.println("command has been completely processed");
					}	
				} catch(java.net.SocketException se) {
					System.out.println("Connection closed by server");
					//currentCommandCompleted=true;
					//synchronized(sync) {sync.notify(); }
				}				
				System.out.println("## Client looping ("+shutdown+"/"+socket.isClosed()+")");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {	
			try{socket.close();}catch(Exception e2) {};
		}
	}
}
