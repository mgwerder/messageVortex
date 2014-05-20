package net.gwerder.java.mailvortex.imap;

import java.io.IOException;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.HashSet;
import java.net.Socket;
import java.net.ServerSocket;
import javax.net.SocketFactory;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.TrustManager;
import javax.net.ssl.SSLContext;
import java.security.SecureRandom;

class ImapServer extends StoppableThread  {
	
	final int port;
	ServerSocket serverSocket;
	ConcurrentSkipListSet<ImapConnection> conn=new ConcurrentSkipListSet<ImapConnection>();
	boolean encrypted=false;
	HashSet<String> suppCiphers;
	final SSLContext context=SSLContext.getInstance("TLS");
	private final TrustManager[] trustAllCerts = new TrustManager[] {  new X509TrustManager() {     
			public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null;	} 
			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {} 
			public void checkServerTrusted( java.security.cert.X509Certificate[] certs, String authType) {}
	}}; 
			
	public ImapServer(boolean encrypted) 
		throws java.security.NoSuchAlgorithmException,java.security.KeyManagementException,java.security.GeneralSecurityException,IOException
	{
		this((encrypted?993:143),encrypted);
	}
	
	public ImapServer(final int port,boolean encrypted) 
		throws java.security.NoSuchAlgorithmException,java.security.KeyManagementException,java.security.GeneralSecurityException,IOException
	{
		java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		this.conn=conn;
		this.port=port;
		this.encrypted=encrypted;
		
		// Determine valid cyphers
		String ks="keystore.jks";
		context.init(new X509KeyManager[] {new CustomKeyManager(ks,"changeme", "mykey3") }, trustAllCerts, new SecureRandom() );
		SSLContext.setDefault(context);
		String[] arr=((SSLServerSocketFactory) context.getServerSocketFactory().getDefault()).getSupportedCipherSuites(); 
		suppCiphers=new HashSet<String>();
		for(int i=0;i<arr.length;i++) {
			boolean supported=true;
			SSLServerSocket serverSocket=null;;
			try{ 
				serverSocket = (SSLServerSocket) context.getServerSocketFactory().getDefault().createServerSocket(port);
				serverSocket.setEnabledCipherSuites(new String[] {arr[i]});
				Executors.newSingleThreadExecutor().execute(new Thread() {
					{
						try{
							Thread.sleep(30);
							SSLSocket cs = (SSLSocket)((SSLSocketFactory) context.getSocketFactory().getDefault()).createSocket("localhost", port);
							cs.close();
						} catch(Exception e) {}
					}
				});
				SSLSocket s=(SSLSocket)serverSocket.accept();
				s.close();
				serverSocket.close();
				serverSocket=null;
			} catch(SSLException e) {
				supported=false;
				// System.out.println(arr[i]+" ("+e+")");
				try{
					serverSocket.close();
				} catch(Exception e2) {};
				serverSocket=null;
			}
			if(supported) {
				suppCiphers.add(arr[i]);
			} else {
			}
		}
		
		// open socket
		this.serverSocket = (ServerSocket)ServerSocketFactory.getDefault().createServerSocket(port);
		System.out.println("Server ready..." + serverSocket);	
		Executors.newSingleThreadExecutor().execute(this);
	}
	
	public int shutdown() {
		try{
			shutdown=true;
			Socket sso=SocketFactory.getDefault().createSocket("localhost",this.serverSocket.getLocalPort());
			sso.close();
			this.serverSocket.close();
		} catch(Exception e) {};
		return 0;
	}
	
	public void run() {
		Socket socket=null; 
		try {
			while(!shutdown) {
				socket = serverSocket.accept();
				ImapConnection imc=null;
				imc=new ImapConnection(socket,context,suppCiphers,encrypted);
				conn.add(imc);
				socket=null;
			}
			serverSocket.close();
		} catch (IOException e) {
			if(socket!=null) {
				try{
					socket.close();
				} catch(IOException e2) {} ;
			}    
			e.printStackTrace();
		}
	}
}
//SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(socket,socket.getInetAddress().getHostAddress(),socket.getPort(),true);

