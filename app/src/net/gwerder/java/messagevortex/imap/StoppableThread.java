package net.gwerder.java.messagevortex.imap;

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
