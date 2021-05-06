package net.messagevortex.commandline;

import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.encryption.DumpType;
import picocli.CommandLine;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.logging.Level;

@CommandLine.Command(
    description = "Create an empty IdentityStore",
    name = "create",
    aliases = {"cr"},
    mixinStandardHelpOptions = true
)
public class CommandLineHandlerIdentityStoreCreate implements Callable<Integer> {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @CommandLine.Option(names = {"--filename", "-f"},
      description = "filename of the IdentityStorage file")
  String filename = CommandLineHandlerIdentityStore.DEFAULT_FILENAME;

  /**
   * <p>Commandline handler to create an identity  store.</p>
   *
   * <p>Use java -jar MessageVortex.jar identitystore create --help to get all supported
   * parameters.</p>
   *
   * @return the error level to be returned
   * @throws Exception if anything goes wrong
   */
  @Override
  public Integer call() throws Exception {
    IdentityStore is = new IdentityStore();
    if (new File(filename).exists()) {
      LOGGER.log(Level.SEVERE, "File \"" + filename + "\" already exists");
      return MessageVortex.ARGUMENT_FAIL;
    }
    LOGGER.log(Level.INFO, "Writing \"" + filename + "\"");
    try(OutputStream os = Files.newOutputStream(Paths.get(filename))) {
      os.write(is.toBytes(DumpType.ALL_UNENCRYPTED));
    }
    LOGGER.log(Level.INFO, "Finished");
    return 0;
  }
}
