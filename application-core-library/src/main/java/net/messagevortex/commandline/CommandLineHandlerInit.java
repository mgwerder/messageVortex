package net.messagevortex.commandline;

import net.messagevortex.MessageVortexConfig;
import net.messagevortex.MessageVortexLogger;
import picocli.CommandLine;

import java.util.concurrent.Callable;
import java.util.logging.Level;


@CommandLine.Command(
    description = "initialize or upgrade configuration files",
    name = "initconfig",
    aliases = {"icfg"},
    mixinStandardHelpOptions = true
)
public class CommandLineHandlerInit implements Callable<Integer> {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @CommandLine.Option(names = {"--filename", "-f"},
      description = "filename of the config file")
  String filename = MessageVortexConfig.DEFAULT_FILENAME;

  @CommandLine.Option(names = {"--newname", "-n"},
      description = "filename of the new config file")
  String nfilename = null;

  /***
   * <p>Commandline handler to rewrite a commented configuration file.</p>
   *
   * @return the error level
   * @throws Exception if problem when obtaining logger
   */
  public Integer call() throws Exception {
    LOGGER.log(Level.INFO, "init called");
    LOGGER.log(Level.INFO, "reading " + filename);
    MessageVortexConfig.getDefault().load(filename);
    if (nfilename == null) {
      nfilename = filename;
    }
    LOGGER.log(Level.INFO, "writing new  " + nfilename);
    MessageVortexConfig.getDefault().store(nfilename);
    return 0;
  }

}
