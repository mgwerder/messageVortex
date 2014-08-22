package net.gwerder.java.mailvortex.imap;

import net.gwerder.java.mailvortex.MailvortexLogger;
import java.util.logging.Logger;
import java.util.logging.Level;
  
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

class SocketDeblocker extends Thread {

    private static final Logger LOGGER;
    
    static {
        LOGGER = MailvortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }
    
    private int timeout;
    private int port;
    private boolean shutdown=false;

    public SocketDeblocker(int port,int timeout) { 
        this.port=port;
        this.timeout=timeout;
    }    
    
    public void shutdown() {
        shutdown=true;
        while(this.isAlive()) {
            try{
                this.join();
            }catch(InterruptedException ie) {
				LOGGER.log(Level.INFO,"Interrupted exception while shutting down deblocking socket",ie);
            }
        }    
    }
    
    public void run() {
        int countdown=timeout/10;
        while(!shutdown && countdown>0) {
            countdown--;
            try{
                Thread.sleep(10);
            }catch(InterruptedException ie) {
				LOGGER.log(Level.INFO,"Interrupted exception while running SocketDeblocker",ie);
            }
        }

        if(!shutdown) {
            try{
                SSLSocket cs = (SSLSocket)SSLSocketFactory.getDefault().createSocket("localhost", port);
                cs.close();
            } catch(Exception e) {
				LOGGER.log(Level.INFO,"Exception while running SocketDeblocker",e);
            }
        }
    }
    
}