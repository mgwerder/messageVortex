package net.gwerder.java.mailvortex.imap;
 
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.net.Socket;
import javax.net.SocketFactory;
import java.net.ServerSocket;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.SSLContext;
import java.security.SecureRandom;
import java.security.KeyStore;
 
/**
 * @author Martin Gwerder
 * FIXME Trust and keystoreimplementation broken
 */
public class ImapSSLSocket {

	/**
     * Selftest function for IMAP server.
     *
     * @param args      ignored commandline arguments
     */
    public static void main(String[] args) throws Exception {
		boolean encrypted=true;
        Server s=new Server(143,encrypted);
        new Client("localhost",143,encrypted);
    }
	
	static class ImapConnection extends StoppableThread implements Comparable<ImapConnection> {
	
		static private final int CONNECTION_NOT_AUTHENTICATED = 1;
		static private final int CONNECTION_AUTHENTICATED     = 2;
		static private final int CONNECTION_SELECTED          = 3;
		
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
				startTLS();
				if(sslSocket!=null) sslSocket.close();
				plainSocket.close();
			} catch(IOException e) {
				e.printStackTrace();
				try{
					if(sslSocket!=null) sslSocket.close();
					plainSocket.close();
				}catch(Exception e2) {}
			}	
		}
		
	}
 
    static class Server extends StoppableThread  {
        
        private int port;
        ServerSocket serverSocket;
		Set<ImapConnection> conn=new ConcurrentSkipListSet<ImapConnection>();
		boolean encrypted=false;
		Set<String> suppCiphers;
		final SSLContext context=SSLContext.getInstance("TLS");
		private final TrustManager[] trustAllCerts = new TrustManager[] {  new X509TrustManager() {     
				public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null;	} 
				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {} 
				public void checkServerTrusted( java.security.cert.X509Certificate[] certs, String authType) {}
		}}; 
                
        public Server(int port,boolean encrypted) 
			throws java.security.NoSuchAlgorithmException,java.security.KeyManagementException,java.security.GeneralSecurityException,IOException
		{
			java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			this.conn=conn;
            this.port=port;
			this.encrypted=encrypted;
			
			// Determine valid cyphers
			String ks="keystore.jks";
			context.init(new X509KeyManager[] {new CustomKeyManager(ks,"changeme", "mykey3") }, trustAllCerts, new SecureRandom() );
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
								SSLSocket cs = (SSLSocket)((SSLSocketFactory) context.getSocketFactory().getDefault()).createSocket("localhost", 143);
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
					System.out.println(arr[i]+" ("+e+")");
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

	
 
    static class Client implements Runnable {
	
		private String targetHost="localhost";
		private int targetPort = 143;
		private boolean encrypted;
		private final TrustManager[] trustAllCerts = new TrustManager[] {  new X509TrustManager() {     
				public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null;	} 
				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {} 
				public void checkServerTrusted( java.security.cert.X509Certificate[] certs, String authType) {}
		}}; 
		
		public Client(String targetHost,int targetPort,boolean encrypted) {
			this.targetHost=targetHost;
			this.targetPort=targetPort;
			this.encrypted=encrypted;
			Executors.newSingleThreadExecutor().execute(this);
		}
	
		private Socket startTLS(Socket sock) throws IOException,java.security.NoSuchAlgorithmException,java.security.KeyManagementException,java.security.KeyStoreException,java.security.cert.CertificateException {
			java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			KeyStore trustStore = KeyStore.getInstance("jks");
			trustStore.load(ImapSSLSocket.class.getClassLoader().getSystemResourceAsStream("keystore.jks"), "changeme".toCharArray());
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(trustStore);
			SSLContext trustContext=SSLContext.getInstance("TLS");
			trustContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom() );
			trustContext.init(null, trustAllCerts, new java.security.SecureRandom()); 
			SSLSocket sslSocket = (SSLSocket)(((SSLSocketFactory)(trustContext.getSocketFactory().getDefault())).createSocket(sock,sock.getInetAddress().getHostAddress(),sock.getPort(), false));    
			sslSocket.setUseClientMode(true);    
			sslSocket.startHandshake();
			return sslSocket;
		}
	
        public void run() {
			Socket socket=null;
            try {
                socket = SocketFactory.getDefault().createSocket(targetHost,targetPort);
				socket=startTLS(socket);
				String[] arr=((SSLSocket)(socket)).getEnabledCipherSuites();
				for(int i=0;i<arr.length;i++) System.out.println(" supported by client: "+arr[i]);
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
}