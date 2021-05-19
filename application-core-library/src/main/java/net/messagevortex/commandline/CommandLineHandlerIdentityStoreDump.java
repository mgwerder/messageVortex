package net.messagevortex.commandline;

import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.NotImplementedException;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.encryption.DumpType;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.logging.Level;

/**
 * <p>Commandline handler for dumping the identity store.</p>
 */
@CommandLine.Command(
    description = "dump content of IdentityStore",
    name = "dump",
    aliases = {"dp"},
    mixinStandardHelpOptions = true
)
public class CommandLineHandlerIdentityStoreDump implements Callable<Integer> {

  enum Format {
    ASN1,
    YAML
  }

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @CommandLine.Option(names = {"--filename", "-f"},
      description = "filename of the IdentityStorage file",
      arity = "1"
  )
  String filename = CommandLineHandlerIdentityStore.DEFAULT_FILENAME;


  @CommandLine.Option(names = {"--outputFormat", "-o"},
      description = "Format of output"
  )
  Format outputFormat = Format.ASN1;

  /**
   * <p>Commandline handler to dump identities of an identity store.</p>
   *
   * <p>Use java -jar MessageVortex.jar identitystore dump --help to get all supported
   * parameters.</p>
   *
   * @return the error level to be returned
   * @throws Exception if anything goes wrong
   */
  @Override
  public Integer call() throws Exception {
    if (!(new File(filename).exists())) {
      LOGGER.log(Level.SEVERE, "File \"" + filename + "\" not found");
      return MessageVortex.ARGUMENT_FAIL;
    }
    IdentityStore is = new IdentityStore(new File(filename));
    String out = "";
    if (outputFormat == Format.ASN1) {
      out = is.dumpValueNotation("", DumpType.ALL_UNENCRYPTED);
    } else {
      throw new NotImplementedException();
    }
    System.out.println(out);
    return 0;
  }
}
