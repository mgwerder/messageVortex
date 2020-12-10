package net.messagevortex.commandline;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import net.messagevortex.MessageVortexConfig;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.NotImplementedException;
import picocli.CommandLine;


/**
 * <p>Commandline handler to generate sample files.</p>
 *
 * <p>Implements the command "example".</p>
 */
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
public class CommandLineHandlerExamples {

  public static final String DEFAULT_FILENAME = "identityStore.cfg";

}
