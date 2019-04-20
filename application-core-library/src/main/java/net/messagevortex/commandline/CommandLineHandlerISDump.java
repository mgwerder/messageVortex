package net.messagevortex.commandline;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.encryption.DumpType;
import picocli.CommandLine;

@CommandLine.Command(
        description = "dump content of IdentityStore",
        name = "dump",
        aliases = {"dp"},
        mixinStandardHelpOptions = true
)
public class CommandLineHandlerISDump implements Callable<Integer> {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @CommandLine.Option(names = {"--filename", "-f"},
          description = "filename of the IdentityStorage file",
          arity = "1"
  )
  String filename = CommandLineHandlerIS.DEFAULT_FILENAME;

  @Override
  public Integer call() throws Exception {
    if (!(new File(filename).exists())) {
      LOGGER.log(Level.SEVERE, "File \"" + filename + "\" not found");
      return MessageVortex.ARGUMENT_FAIL;
    }
    IdentityStore is = new IdentityStore(new File(filename));
    System.out.println(is.dumpValueNotation("",DumpType.ALL_UNENCRYPTED));
    return null;
  }
}
