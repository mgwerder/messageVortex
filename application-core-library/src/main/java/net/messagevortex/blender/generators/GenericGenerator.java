package net.messagevortex.blender.generators;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.BlendingParameter;
import net.messagevortex.asn1.VortexMessage;
import net.messagevortex.blender.BlenderContent;

public class GenericGenerator implements BlenderGenerator {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }


  @Override
  public BlenderContent getBlenderContent(BlendingParameter parameter, VortexMessage msg)
      throws IOException {
    BlenderContent cont = new BlenderContent();
    cont.setText("Hi\n\nHave you seen that one? Nice image!\n\nLove\nTeddy");
    File f = new File("images/F5Blender");
    cont.addAttachment(getFileContent(getRandomFile(f)));
    return cont;
  }

  /**
   * <p>Returns a random file from the given directory.</p>
   *
   * @param dir the directory containing the random files
   * @return a random file of the directory
   */
  private static File getRandomFile(File dir) throws IOException {
    File[] fa = dir.listFiles();
    if (fa == null || fa.length == 0) {
      throw new IOException("Directory \"" + dir
          + "\" is empty... there is no file to choose from");
    }
    ExtendedSecureRandom sr = new ExtendedSecureRandom();
    return fa[ExtendedSecureRandom.nextInt(fa.length)];
  }

  private static byte[] getFileContent(File f) {
    byte[] b = null;
    try (InputStream is = new FileInputStream(f.toString())) {
      b = new byte[(int) (f.length())];
      is.read(b);
    } catch (IOException ioe) {
      LOGGER.log(Level.WARNING, "unable to reat file \"" + f.getAbsolutePath() + "\"", ioe);
    }
    return b;
  }
}
