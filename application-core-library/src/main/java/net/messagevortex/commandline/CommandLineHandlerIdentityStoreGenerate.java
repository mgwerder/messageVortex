package net.messagevortex.commandline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.AsymmetricKey;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.IdentityStoreBlock;
import net.messagevortex.asn1.UsagePeriod;
import net.messagevortex.asn1.encryption.Algorithm;
import net.messagevortex.asn1.encryption.AlgorithmType;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.asn1.encryption.SecurityLevel;
import picocli.CommandLine;

@CommandLine.Command(
        description = "generate a new identity and dump store",
        name = "generate",
        aliases = {"gen"},
        mixinStandardHelpOptions = true
)
public class CommandLineHandlerIdentityStoreGenerate implements Callable<Integer> {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @CommandLine.Option(names = {"--filename", "-f"}, required = false,
          description = "filename of the IdentityStorage file")
  String filename = CommandLineHandlerIdentityStore.DEFAULT_FILENAME;

  @CommandLine.Option(names = {"--identityName", "-i"}, required = true,
          description = "name of the identity")
  String identityId;

  @Override
  public Integer call() throws Exception {
    // load store
    if (!new File(filename).exists()) {
      LOGGER.log(Level.SEVERE, "File \"" + filename + "\" not found");
      return MessageVortex.ARGUMENT_FAIL;
    }
    LOGGER.log(Level.INFO, "Loading identity store \"" + filename + "\"");
    IdentityStore is = new IdentityStore(new File(filename));

    // generate new identity
    LOGGER.log(Level.INFO, "Generating new identity for \"" + identityId + "\"");
    IdentityStoreBlock isb = new IdentityStoreBlock();
    isb.setValid(new UsagePeriod(new Date(),new Date( new Date().getTime() * 3600 * 24 * 365))); // set validity to one year
    isb.setTransferQuota(ExtendedSecureRandom.nextInt(Integer.MAX_VALUE)); // maximum transfer quota
    isb.setMessageQuota(ExtendedSecureRandom.nextInt(Integer.MAX_VALUE)); // maximum Message quota
    isb.setIdentityKey(new AsymmetricKey(
            Algorithm.getDefault(AlgorithmType.ASYMMETRIC).getParameters(SecurityLevel.QUANTUM))
    );
    isb.setNodeAddress("smtp:" + identityId);
    isb.setNodeKey(null);
    is.add(isb);

    // dump store
    LOGGER.log(Level.INFO, "Dumping identity store");
    System.out.println(is.dumpValueNotation("",DumpType.ALL_UNENCRYPTED));

    // dump store to disk
    LOGGER.log(Level.INFO, "writing identity store to \"" + filename + "\"");
    OutputStream os = new FileOutputStream(filename);
    os.write(is.toBytes(DumpType.ALL_UNENCRYPTED));
    os.close();

    LOGGER.log(Level.INFO, "finished");
    return 0;
  }
}
