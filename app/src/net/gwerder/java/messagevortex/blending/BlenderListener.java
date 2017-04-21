package net.gwerder.java.messagevortex.blending;

import java.io.InputStream;

/**
 * Created by Martin on 21.04.2017.
 */
public interface BlenderListener {

    /***
     * This Method is called by the Blending layer if a vmessage has been extracted.
     *
     * @param is the InputStream containing a vmessage
     */
    public void gotMessage(InputStream is);

}
