package net.gwerder.java.mailvortex.imap;
 
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.SSLContext;
 
/**
 * @author Martin Gwerder
 */
public class ImapSSLSocket {

	/**
     * Selftest function for IMAP server.
     *
     * @param args      ignored commandline arguments
     */
    public static void main(String[] args) throws Exception {
        Server s=new Server(143);
        Executors.newSingleThreadExecutor().execute(new Client());
    }
	
	static class ImapConnection extends StoppableThread implements Comparable<ImapConnection> {
	
		static private final int CONNECTION_NOT_AUTHENTICATED = 1;
		static private final int CONNECTION_AUTHENTICATED     = 2;
		static private final int CONNECTION_SELECTED          = 3;
		
		SSLSocket sock;
		private int status=CONNECTION_NOT_AUTHENTICATED;
		
		public ImapConnection(SSLSocket sock) 
		{
			this.sock=sock;	
            SSLSession sslSession = sock.getSession();
            String cipherSuite = sslSession.getCipherSuite();
            System.out.println("Server got connection secured with "+cipherSuite);
			Executors.newSingleThreadExecutor().execute(this);
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
        
        public void run() {
			try{
				Scanner scanner = new Scanner(sock.getInputStream());
				System.out.println("Reading...");
				String got="";
				try{
					while (true) {
						try{
							System.out.println("waiting for full line");
							while(!scanner.hasNextLine()) { 
								Thread.sleep(10); 
							}
						} catch(InterruptedException e) {}	
						got=scanner.nextLine();
						System.out.println("Server received: " + got);
					}
				} catch(RuntimeException e) {}	
				System.out.println("Server stopped reading...");
				scanner.close();
				sock.close();
			} catch(IOException e) {
				e.printStackTrace();
			}	
		}
		
	}
 
    static class ImapListener extends StoppableThread  {
        
        private int port=143;
        SSLServerSocket serverSocket;
		Set<ImapConnection> conn;
        
        public ImapListener(int port,Set<ImapConnection> conn) 
			throws java.security.NoSuchAlgorithmException,java.security.KeyManagementException,java.security.GeneralSecurityException,IOException
		{
			this.conn=conn;
            this.port=port;
			SSLContext sctx=SSLContext.getInstance("TLS"  );
			sctx.init(new X509KeyManager[] {new CustomKeyManager("../../keystore.jks","changeme", "mykey2") }, null, null);
            System.out.println("Server ready..." + serverSocket);	
            System.out.println("Supported Cipher Suites: ");
			String[] arr=((SSLServerSocketFactory) sctx.getServerSocketFactory().getDefault()).getSupportedCipherSuites(); 
			HashSet<String> suppCiphers=new HashSet<String>();
			for(int i=0;i<arr.length;i++) {
				System.out.print("   - "+arr[i]+" ");	
				boolean supported=true;
				try{ 
					this.serverSocket = (SSLServerSocket) sctx.getServerSocketFactory().getDefault().createServerSocket(port);
					this.serverSocket.setEnabledCipherSuites(new String[] {arr[i]});
					// Fix blocking socket
					Executors.newSingleThreadExecutor().execute(new Thread() {
						{
							try{
								Thread.sleep(100);
								SSLSocket cs = (SSLSocket) SSLSocketFactory.getDefault().createSocket("localhost", 143);
							} catch(Exception e) {}
						}
					});
					SSLSocket s=(SSLSocket)serverSocket.accept();
					s.close();
				} catch(SSLException e) {supported=false;}
				if(supported) {
					suppCiphers.add(arr[i]);
					System.out.println("(appropriate key found)");
				} else {
					System.out.println("(NO KEY)");
				}
				this.serverSocket.close();
			}
            this.serverSocket = (SSLServerSocket) sctx.getServerSocketFactory().getDefault().createServerSocket(port);
			for(int i=0;i<suppCiphers.toArray().length;i++) {
				System.out.println("Supported cypher: "+(suppCiphers.toArray(new String[0]))[i]);
			}
			this.serverSocket.setEnabledCipherSuites(suppCiphers.toArray(new String[0]));
        }
        
        public int shutdown() {
            try{
				this.serverSocket.close();
			} catch(Exception e) {};
            return 0;
        }
        
        public void run() {
            SSLSocket socket=null; 
            Scanner scanner=null;
            try {
                while(!shutdown) {
                    socket = (SSLSocket) serverSocket.accept();
                    // FIXME fork scanner thread off
					ImapConnection imc=null;
					imc=new ImapConnection(socket);
					conn.add(imc);
                    socket=null;
                }
                serverSocket.close();
            } catch (IOException e) {
                if(scanner!=null) scanner.close();
                if(socket!=null) {
                    try{
                        socket.close();
                    } catch(IOException e2) {} ;
                }    
                e.printStackTrace();
            }
        }
        
        private boolean starttls() {
            // FIXME implementation missing
            return false;
        }
    }
    //SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(socket,socket.getInetAddress().getHostAddress(),socket.getPort(),true);

    static class Server {
        
		static Set<ImapConnection> conn=new ConcurrentSkipListSet<ImapConnection>();
        private int port;
        private ImapListener listener;
        
        public Server() 
			throws java.security.NoSuchAlgorithmException,java.security.KeyManagementException,java.security.GeneralSecurityException,IOException
		{
            this(143);
        }
        
        public Server(int port) 
			throws java.security.NoSuchAlgorithmException,java.security.KeyManagementException,java.security.GeneralSecurityException,IOException
		{
            this.port = port;
            listener  = new ImapSSLSocket.ImapListener(port,conn);
            Executors.newSingleThreadExecutor().execute(listener);
        }
        
    }
	
 
    static class Client implements Runnable {
        public void run() {
            try {
 
                SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket("localhost", 143);
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                System.out.println("Client -> sending...");
                for (int i = 0; i < 5; i++) {
                    String message = "Hallo " + i;
                    System.out.println("Client sending: " + message);
                    printWriter.println(message+"\r\n");
                    printWriter.flush();
                    System.out.println("Client sent: " + message);
                    System.out.println("sleeping...");
                    TimeUnit.SECONDS.sleep(1);
                }
                socket.close();
				System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}