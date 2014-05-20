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

class ImapClient implements Runnable {

	private String targetHost="localhost";
	private int targetPort = 143;
	private boolean encrypted;
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

	public void run() {
		Socket socket=null;
		try {
			socket = SocketFactory.getDefault().createSocket(targetHost,targetPort);
			if(encrypted) socket=startTLS(socket);
			System.out.flush();
			socket.getOutputStream().write("test".getBytes());
			socket.close();
			System.out.println("Client -> sending...");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			try{socket.close();}catch(Exception e2) {};

		}
	}
}
