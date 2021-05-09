package net.messagevortex.commandline;

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
