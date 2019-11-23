package net.messagevortex.test.cli;

import static net.messagevortex.MessageVortex.ARGUMENT_FAIL;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexLogger;
import org.junit.Test;

public class GeneralCommandline {

  private static final Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    MessageVortexLogger.setGlobalLogLevel(Level.ALL);
  }

  public static Object[] runCommandline(String[] args) {
    PrintStream out = System.out;
    PrintStream err = System.err;

    out.flush();
    err.flush();

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ByteArrayOutputStream boe = new ByteArrayOutputStream();

    int i = -1;
    Exception e = null;

    try {
      PrintStream newOut = new PrintStream(bos);
      PrintStream newErr = new PrintStream(boe);

      System.setOut(newOut);
      System.setErr(newErr);

      i = MessageVortex.mainReturn(args);

      MessageVortexLogger.flush();

      System.err.close();
      System.out.close();

    } catch (Exception rte) {
      e = rte;
    } finally {
      System.setOut(out);
      System.setErr(err);
    }
    return new Object[]{new Integer(i), bos.toString(), boe.toString(), e};
  }

  @Test
  public void helpIsPrinted() {
    Object[] o = runCommandline(new String[]{"--help"});
    String out = ((String) (o[1]));
    String err = ((String) (o[2]));

    System.out.println("## out: "+out);
    System.out.println("## err: "+err);

    assertTrue("Help text not found (1)", out.contains("Commands:"));
    assertTrue("Help text not found (2)", out.contains("--help"));
    assertTrue("Help text not found (3)", out.contains("--version"));
    // assertTrue("Help text not found (4)", err.contains(Version.getBuild()));
    assertTrue(
            "Return value is not " + ARGUMENT_FAIL,
            ((Integer) (o[0])).intValue() == ARGUMENT_FAIL
    );
  }

}
