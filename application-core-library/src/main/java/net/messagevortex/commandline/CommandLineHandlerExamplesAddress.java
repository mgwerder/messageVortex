package net.messagevortex.commandline;

import java.util.Base64;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.AlgorithmParameter;
import net.messagevortex.asn1.AsymmetricKey;
import net.messagevortex.asn1.encryption.Algorithm;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.asn1.encryption.Parameter;
import net.messagevortex.asn1.encryption.SecurityLevel;
import picocli.CommandLine;

@CommandLine.Command(
        description = "create a sample address",
        name = "address",
        aliases = {"addr"},
        mixinStandardHelpOptions = true
)
public class CommandLineHandlerExamplesAddress implements Callable<Integer> {
  
  private static final java.util.logging.Logger LOGGER;
  
  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }
  
  @CommandLine.Option(names = {"--address", "-a"}, required = false,
          description = "standard address")
  String address = "bob.andrews@example.com";
  
  @CommandLine.Option(names = {"--cypher", "-c"}, required = false,
          description = "the cypher to be used")
  String cypher = "EC";
  
  @CommandLine.Option(names = {"--transport", "-t"}, required = false,
          description = "the transport protocol")
  String protocol = "smtp";
  
  @CommandLine.Option(names = {"--key-size", "-s"}, required = false,
          description = "the key size")
  int size = 384;
  
  @Override
  public Integer call() throws Exception {
    // create key
    LOGGER.log(Level.INFO, "creating custom key for cypher " + cypher);
    Algorithm a = Algorithm.getByString(cypher);
    AlgorithmParameter ap = a.getParameters(SecurityLevel.HIGH);
    ap.put(Parameter.KEYSIZE, "" + size);
    AsymmetricKey key = new AsymmetricKey(ap);
    
    LOGGER.log(Level.INFO, "Address is: vortex" + protocol + "://"
            + address.substring(0, address.indexOf('@')) + ".."
            + Base64.getEncoder().encodeToString(
            key.toAsn1Object(DumpType.PUBLIC_ONLY).getEncoded())
            + ".." + address.substring(address.indexOf('@') + 1) + "@localhost");
    
    LOGGER.log(Level.INFO, "finished");
    return 0;
  }
}
