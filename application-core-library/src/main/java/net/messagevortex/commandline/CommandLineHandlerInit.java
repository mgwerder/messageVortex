package net.messagevortex.commandline;

import net.messagevortex.MessageVortexConfig;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.Version;
import picocli.CommandLine;

import java.util.concurrent.Callable;
import java.util.logging.Level;

@CommandLine.Command(
        description = "initialize or upgrade configuration files",
        name = "initconfig",
        aliases= { "icfg" },
        mixinStandardHelpOptions = true,
        subcommands = {
        }
)
public class CommandLineHandlerInit implements Callable<Integer> {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @CommandLine.Option(names = {"--filename", "-f"}, required = false,
          description = "filename of the config file")
  String filename = MessageVortexConfig.DEFAULT_FILENAME;

  @CommandLine.Option(names = {"--newname", "-n"}, required = false,
          description = "filename of the new config file")
  String nfilename = null;

  public Integer call() throws Exception {
    LOGGER.log(Level.INFO,"init called");
    LOGGER.log(Level.INFO,"reading "+filename);
    MessageVortexConfig.getDefault().load(filename);
    if( nfilename==null) {
      nfilename=filename;
    }
    LOGGER.log(Level.INFO,"writing new  "+nfilename);
    MessageVortexConfig.getDefault().store(nfilename);
    return 0;
  }

}
