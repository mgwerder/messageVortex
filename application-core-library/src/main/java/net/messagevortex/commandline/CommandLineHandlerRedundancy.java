package net.messagevortex.commandline;

import picocli.CommandLine;

/**
 * <p>Commandline handler to the redundancy operations.</p>
 *
 * <p>Implements the command "redundancy".</p>
 */
@CommandLine.Command(
    description = "Helper for add redundancy operations",
    name = "redundancy",
    aliases = {"red"},
    mixinStandardHelpOptions = true,
    subcommands = {
        CommandLineHandlerRedundancyAdd.class
    }
)
public class CommandLineHandlerRedundancy {
}
