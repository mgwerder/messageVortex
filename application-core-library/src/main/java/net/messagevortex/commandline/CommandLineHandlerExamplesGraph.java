package net.messagevortex.commandline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.AlgorithmParameter;
import net.messagevortex.asn1.AsymmetricKey;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.IdentityStoreBlock;
import net.messagevortex.asn1.encryption.Algorithm;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.asn1.encryption.Parameter;
import net.messagevortex.asn1.encryption.SecurityLevel;
import net.messagevortex.router.JGraph;
import net.messagevortex.router.SimpleMessageFactory;
import org.bouncycastle.asn1.DEROutputStream;
import picocli.CommandLine;

@CommandLine.Command(
        description = "create a sample graph",
        name = "graph",
        aliases = {"gr"},
        mixinStandardHelpOptions = true
)
public class CommandLineHandlerExamplesGraph implements Callable<Integer> {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @CommandLine.Option(names = {"--nodes", "-n"}, required = false,
      description = "Number of nodes")
  int numNodes = 5;

  @CommandLine.Option(names = {"--filename", "-f"}, required = false,
      description = "filename")
  String filename = null;

  @CommandLine.Option(names = {"--x-resolution", "-x"}, required = false,
      description = "resolution of x-axis")
  int x = 1024;

  @CommandLine.Option(names = {"--y-resolution", "-y"}, required = false,
      description = "resolution of x-axis")
  int y = 768;

  @CommandLine.Option(names = {"--redundancy-size", "-r"}, required = false,
      description = "minimum number of paths to target node")
  int redundancy = 3;

  @Override
  public Integer call() throws Exception {
    IdentityStore is = null;
    try {
      is = new IdentityStore(new File(System.getProperty("java.io.tmpdir")
          + "/IdentityStoreExample1.der"));
    } catch (IOException ioe) {
      is = IdentityStore.getNewIdentityStoreDemo(false);
      DEROutputStream f = new DEROutputStream(
          new FileOutputStream(
              System.getProperty("java.io.tmpdir") + "/IdentityStoreExample1.der"
          )
      );
      f.writeObject(is.toAsn1Object(DumpType.ALL_UNENCRYPTED));
      f.close();
    }
    SimpleMessageFactory smf = null;
    do {
      smf = new SimpleMessageFactory("", 0, 1,
          is.getAnonSet(numNodes).toArray(new IdentityStoreBlock[0]), is);
      smf.build();
      System.out.println("got " + smf.getGraph().getRoutes().length + " routes");
    } while(smf.getGraph().getRoutes().length < redundancy);

    // store or display graph
    final JGraph jg = new JGraph(smf.getGraph());
    if (filename==null) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          jg.createAndShowUserInterface(x, y);
        }
      });
    } else {
      // store image
      jg.saveScreenshot(filename,x,y);
    }

    return 0;
  }
}
