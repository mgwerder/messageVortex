package net.gwerder.java.mailvortex.imap;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

class SocketDeblocker extends Thread {

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
            }
        }

        if(!shutdown) {
            try{
                SSLSocket cs = (SSLSocket)SSLSocketFactory.getDefault().createSocket("localhost", port);
                cs.close();
            } catch(Exception e) {
            }
        }
    }
    
}