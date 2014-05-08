package net.gwerder.java.mailvortex.imap;
 
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
 
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
 
/**
 * @author Martin Gwerder
 */
public class ImapSSLSocket {
 
    public static final int PORT=5678;
    
    /**
     * Selftest function for IMAP server.
     *
     * @param args      ignored commandline arguments
     */
    public static void main(String[] args) {
        Server s=new Server(143);
        Executors.newSingleThreadExecutor().execute(new Client());
    }
 
    static class ImapListener extends StoppableThread  {
        
        private int port=143;
        SSLServerSocket serverSocket;
        
        public ImapListener(int port) {
            this.port=port;
            try{
                this.serverSocket = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(port);
                System.out.println("Server ready..." + serverSocket);
                System.out.println("Supported Cipher Suites: " + Arrays.toString(((SSLServerSocketFactory) SSLServerSocketFactory.getDefault()).getSupportedCipherSuites()));
            } catch(IOException e) {
                //FIXME unable to allocate port
                e.printStackTrace();
            }
        }
        
        public int shutdown() {
            // FIXME implementation missing
            return 0;
        }
        
        public void run() {
            SSLSocket socket=null; 
            Scanner scanner=null;
            try {
                while(!shutdown) {
                    socket = (SSLSocket) serverSocket.accept();
                    // FIXME fork scanner thread off
                    SSLSession sslSession = socket.getSession();
                    String cipherSuite = sslSession.getCipherSuite();
                    System.out.println(cipherSuite);
                    scanner = new Scanner(socket.getInputStream());
                    System.out.println("Reading...");
                    while (scanner.hasNextLine()) {
                        System.out.println("Server received: " + scanner.nextLine());
                    }
                    scanner.close();
                    socket.close();
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
        
        private int port;
        private ImapListener listener;
        
        public Server() {
            this(143);
        }
        
        public Server(int port) {
            this.port = port;
            listener  = new ImapSSLSocket.ImapListener(port);
            Executors.newSingleThreadExecutor().execute(listener);
        }
        
    }
	
 
    static class Client implements Runnable {
        public void run() {
            try {
 
                SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket("localhost", PORT);
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                System.out.println("Client -> sending...");
                for (int i = 0; i < 5; i++) {
                    String message = "Hallo: " + i;
                    System.out.println("Client sent: " + message);
                    printWriter.println(message);
                    printWriter.flush();
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