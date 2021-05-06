package net.messagevortex.commandline;

import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.router.operation.AddRedundancy;
import picocli.CommandLine;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.logging.Level;


@CommandLine.Command(
        description = "add redundancy",
        name = "addRedundancy",
        aliases = {"add"},
        mixinStandardHelpOptions = true
)
public class CommandLineHandlerRedundancyAdd implements Callable<Integer> {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @CommandLine.Option(names = {"--infile", "-i"}, required = true,
          description = "filename of the file to add redundancy")
  String inFile;

  @CommandLine.Option(names = {"--outfile", "-o"}, required = true,
          description = "output filename")
  String outFile;

  @CommandLine.Option(names = {"--redundancyBlocks"}, required = true,
          description = "the number of blocks which may be removed")
  int redundancyBlocks;

  @CommandLine.Option(names = {"--blocks"}, required = true,
          description = "the number of blocks")
  int numBlocks;

  @CommandLine.Option(names = {"--gf"}, required = true,
          description = "the size of the gauloise field in bits")
  int gf;

  /**
   * <p>Commandline handler to apply an add redundancy operation to a file.</p>
   *
   * <p>Use java -jar MessageVortex.jar redundancy addRedundancy --help to get all supported
   * parameters.</p>
   *
   * @return the error level to be returned
   * @throws Exception if anything goes wrong
   */
  @Override
  public Integer call() throws Exception {
    // load store
    if (!new File(inFile).exists()) {
      LOGGER.log(Level.SEVERE, "File \"" + inFile + "\" not found");
      return MessageVortex.ARGUMENT_FAIL;
    }
    LOGGER.log(Level.INFO, "Loading file \"" + inFile + "\"");
    byte[] buffer = new byte[(int) new File(inFile).length()];
    try (InputStream fis = Files.newInputStream(Paths.get(inFile))) {
      fis.read(buffer);
    }

    // apply redundancy operation
    LOGGER.log(Level.INFO, "adding redundancy");
    byte[] out = AddRedundancy.execute(buffer, redundancyBlocks, numBlocks, gf);

    // write output file
    LOGGER.log(Level.INFO, "writing output");
    try (OutputStream fos = Files.newOutputStream(Paths.get(outFile))) {
      fos.write(out);
    }

    LOGGER.log(Level.INFO, "finished");
    return 0;
  }
}
