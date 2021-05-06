package net.messagevortex.commandline;

import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.encryption.DumpType;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.logging.Level;

@CommandLine.Command(
        description = "remove an identity and dump store",
        name = "removeIdentity",
        aliases = {"remove","rem","del"},
        mixinStandardHelpOptions = true
)
public class CommandLineHandlerIdentityStoreDel implements Callable<Integer> {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @CommandLine.Option(names = {"--filename", "-f"},
          description = "filename of the IdentityStorage file")
  String filename = CommandLineHandlerIdentityStore.DEFAULT_FILENAME;

  @CommandLine.Option(names = {"--nodeAddress"}, required = true,
          description = "the identity address")
  String[] nodeAddress;

  /**
   * <p>Commandline handler to remove an identity from an identity store.</p>
   *
   * <p>Use java -jar MessageVortex.jar identitystore removeIdentity --help to get all supported
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

    // remove identity
    for (String v:nodeAddress) {
      LOGGER.log(Level.INFO, "Removing \"" + v + "\"");
      try {
        is.removeAddress(v);
      } catch (IOException ioe) {
        LOGGER.log(Level.SEVERE, "unable to remove \"" + v + "\"");
        return MessageVortex.CONFIG_FAIL;
      }
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
