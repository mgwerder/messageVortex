package net.messagevortex.test.cli;

import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.test.GlobalJunitExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.Logger;

import static net.messagevortex.MessageVortex.ARGUMENT_FAIL;


@ExtendWith(GlobalJunitExtension.class)
public class GeneralCommandline {

  private static final Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
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

    Assertions.assertTrue(out.contains("Commands:"), "Help text not found (1)");
    Assertions.assertTrue(out.contains("--help"), "Help text not found (2)");
    Assertions.assertTrue(out.contains("--version"), "Help text not found (3)");
    // assertTrue("Help text not found (4)", err.contains(Version.getBuild()));
    Assertions.assertTrue(
            ((Integer) (o[0])).intValue() == ARGUMENT_FAIL,
            "Return value is not " + ARGUMENT_FAIL);
  }

}
