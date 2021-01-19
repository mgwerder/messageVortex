package net.messagevortex.commandline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import javax.swing.*;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.IdentityStoreBlock;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.router.JGraph;
import net.messagevortex.router.SimpleMessageFactory;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1OutputStream;
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
  int xResolution = 1024;
  
  @CommandLine.Option(names = {"--y-resolution", "-y"}, required = false,
          description = "resolution of x-axis")
  int yResolution = 768;
  
  @CommandLine.Option(names = {"--redundancy-size", "-r"}, required = false,
          description = "minimum number of paths to target node")
  int redundancy = 3;
  
  /**
   * <p>Commandline handler to create sample graphs.</p>
   *
   * <p>Use java -jar MessageVortex.jar example graph --help to get all supported parameters.</p>
   *
   * @return the error level to be returned
   * @throws Exception if anything goes wrong
   */
  @Override
  public Integer call() throws Exception {
    IdentityStore is = null;
    try {
      is = new IdentityStore(new File(System.getProperty("java.io.tmpdir")
              + "/IdentityStoreExample1.der"));
    } catch (IOException ioe) {
      is = IdentityStore.getNewIdentityStoreDemo(false);
      ASN1OutputStream f = ASN1OutputStream.create(
              new FileOutputStream(
                      System.getProperty("java.io.tmpdir") + "/IdentityStoreExample1.der"
              ), ASN1Encoding.DER
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
    } while (smf.getGraph().getRoutes().length < redundancy);
    
    // store or display graph
    final JGraph jg = new JGraph(smf.getGraph());
    if (filename == null) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          jg.createAndShowUserInterface(xResolution, yResolution);
        }
      });
    } else {
      // store image
      jg.saveScreenshot(filename, xResolution, yResolution);
    }
    
    return 0;
  }
}
