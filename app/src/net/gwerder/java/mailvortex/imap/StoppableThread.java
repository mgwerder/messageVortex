package net.gwerder.java.mailvortex.imap;

/***
 * Stoppable Thread
 ***/
abstract class StoppableThread extends Thread implements Runnable {

    protected boolean shutdown=false;

    /***
     * Shuts the thread gracefully down.
     ***/
    abstract int shutdown();

}
