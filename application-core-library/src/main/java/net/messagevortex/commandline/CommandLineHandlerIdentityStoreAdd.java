package net.messagevortex.commandline;

import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.IdentityStoreBlock;
import net.messagevortex.asn1.encryption.DumpType;
import picocli.CommandLine;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.logging.Level;

/**
 * <p>Commandline helper to add an identity to an identity store.</p>
 */
@CommandLine.Command(
        description = "add an identity and dump store",
        name = "addIdentity",
        aliases = {"add"},
        mixinStandardHelpOptions = true
)
public class CommandLineHandlerIdentityStoreAdd implements Callable<Integer> {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @CommandLine.Option(names = {"--filename", "-f"},
          description = "filename of the IdentityStorage file")
  String filename = CommandLineHandlerIdentityStore.DEFAULT_FILENAME;

  @CommandLine.Option(names = {"--identity"}, required = true,
          description = "the identity URL")
  String[] identity;

  /**
   * <p>Commandline handler to add an identity to an identity store.</p>
   *
   * <p>Use java -jar MessageVortex.jar identitystore addIdentity --help to get all supported
   * parameters.</p>
   *
   * @return the error level to be returned
   * @throws Exception if anything goes wrong
   */
  @Override
  public Integer call() throws Exception {
    // load store
    if (!new File(filename).exists()) {
      LOGGER.log(Level.SEVERE, "File \"" + filename + "\" not found");
      return MessageVortex.ARGUMENT_FAIL;
    }
    LOGGER.log(Level.INFO, "Loading identity store \"" + filename + "\"");
    IdentityStore is = new IdentityStore(new File(filename));

    // add new identity
    for (String id:identity) {
      LOGGER.log(Level.INFO, "Adding new identity for \"" + id + "\"");
      IdentityStoreBlock isb = new IdentityStoreBlock(id);
      is.add(isb);
    }

    // dump store
    LOGGER.log(Level.INFO, "Dumping identity store");
    System.out.println(is.dumpValueNotation("",DumpType.ALL_UNENCRYPTED));

    // dump store to disk
    LOGGER.log(Level.INFO, "writing identity store to \"" + filename + "\"");
    try(OutputStream os = Files.newOutputStream(Paths.get(filename))) {
      os.write(is.toBytes(DumpType.ALL_UNENCRYPTED));
    }

    LOGGER.log(Level.INFO, "finished");
    return 0;
  }
}
