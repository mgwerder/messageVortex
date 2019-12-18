package net.messagevortex.commandline;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.encryption.Algorithm;
import picocli.CommandLine;

@CommandLine.Command(
        description = "Add key to cache",
        name = "cache",
        aliases = {"cache"},
        mixinStandardHelpOptions = true
)
public class CommandLineHandlerCacheCalculate implements Callable<Integer> {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @CommandLine.Option(names = {"--infile", "-i"},
          description = "filename of the file to handle")
  String inFile="source.txt";

  @CommandLine.Option(names = {"-c", "--ciphertype"}, required = true,
          description = "filename of the file to handle")
  String cipherType;

  @Override
  public Integer call() throws Exception {
    byte[] inBuffer = Files.readAllBytes(new File(inFile).toPath());
    byte[] key = null;
    if (inBuffer != null) {
      key = Files.readAllBytes(new File(inFile).toPath());
    }

    Algorithm ct = Algorithm.getByString(cipherType);
    if (ct == null) {
      throw new IOException("Unknown cipher type");
    }

    // FIXME: code incomplete

    return 0;
  }
}
