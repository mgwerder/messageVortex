package net.messagevortex.commandline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.encryption.DumpType;
import picocli.CommandLine;

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

  @Override
  public Integer call() throws Exception {
    IdentityStore is = new IdentityStore();
    if (new File(filename).exists()) {
      LOGGER.log(Level.SEVERE, "File \"" + filename + "\" already exists");
      return MessageVortex.ARGUMENT_FAIL;
    }
    LOGGER.log(Level.INFO, "Writing \"" + filename + "\"");
    OutputStream os = new FileOutputStream(filename);
    os.write(is.toBytes(DumpType.ALL_UNENCRYPTED));
    os.close();
    LOGGER.log(Level.INFO, "Finished");
    return 0;
  }
}
