package net.messagevortex.commandline;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import net.messagevortex.MessageVortexConfig;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.NotImplementedException;
import picocli.CommandLine;


@CommandLine.Command(
    description = "Create example snippets",
    name = "example",
    aliases = {"ex"},
    mixinStandardHelpOptions = true,
    subcommands = {
        CommandLineHandlerExamplesAddress.class,
        CommandLineHandlerExamplesGraph.class
    }
)
public class CommandLineHandlerExamples implements Callable<Integer> {
  public static final String DEFAULT_FILENAME = "identityStore.cfg";

  public Integer call() throws Exception {
    throw new NotImplementedException();
  }
}
