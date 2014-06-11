package net.gwerder.java.mailvortex.imap;
 
import java.net.Socket;
import javax.net.ssl.SSLSocket;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.FileInputStream;
import javax.net.ssl.TrustManagerFactory;
import java.util.concurrent.Executors;
import java.security.KeyStore;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
//import java.lang.InterruptedException;

class ImapClient implements Runnable {

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
	
	public ImapClient(String targetHost,int targetPort,boolean encrypted) {
		this.targetHost=targetHost;
		this.targetPort=targetPort;
		this.encrypted=encrypted;
		Executors.newSingleThreadExecutor().execute(this);
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
		sslSocket.startHandshake();
		System.out.println("CLientTLS Started");
		String[] arr=((SSLSocket)(sslSocket)).getEnabledCipherSuites();
		for(int i=0;i<arr.length;i++) System.out.println(" supported by client: "+arr[i]);
		return sslSocket;
	}
	
	public String[] sendCommand(String command) throws TimeoutException { return sendCommand(command,DEFAULT_TIMEOUT); }
	
	public String[] sendCommand(String command,int millisTimeout) throws TimeoutException {
		synchronized(sync) {
			currentCommand=command;
			long start = System.currentTimeMillis();
			currentCommandCompleted=false;
			System.out.println("IMAP-> C: "+command);
			synchronized(notifyThread) {notifyThread.notify(); }
			while(!currentCommandCompleted && System.currentTimeMillis()<start+millisTimeout) {
				try{sync.wait(100);} catch(InterruptedException e) {};
			}
			if(!currentCommandCompleted && System.currentTimeMillis()>start+millisTimeout) throw new TimeoutException("Timeout reached while sending \""+command+"\"");
		}
		return new String[0];
	}

	public void run() {
		Socket socket=null;
		try {
			socket = SocketFactory.getDefault().createSocket(targetHost,targetPort);
			if(encrypted) socket=startTLS(socket);
			while(!shutdown) {
				synchronized(notifyThread) {try{notifyThread.wait(100);} catch(InterruptedException e) {} }
				if(currentCommand!=null && !currentCommand.equals("")) {
					socket.getOutputStream().write((currentCommand+"\r\n").getBytes());
					socket.getOutputStream().flush();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {	
			try{socket.close();}catch(Exception e2) {};
			//if(sslSocket!=null) try{sslSocket.close();}catch(Exception e2) {};
		}
	}
}
