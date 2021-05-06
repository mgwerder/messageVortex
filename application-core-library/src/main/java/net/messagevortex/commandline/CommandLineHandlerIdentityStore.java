package net.messagevortex.commandline;

import picocli.CommandLine;

/**
 * <p>commandline handler for identity store manipulations.</p>
 */
@CommandLine.Command(
        description = "Manipulator for IdentityStore",
        name = "intentitystore",
        aliases = { "store","is" },
        mixinStandardHelpOptions = true,
        subcommands = {
                CommandLineHandlerIdentityStoreCreate.class,
                CommandLineHandlerIdentityStoreGenerate.class,
                CommandLineHandlerIdentityStoreAdd.class,
                CommandLineHandlerIdentityStoreDel.class,
                CommandLineHandlerIdentityStoreDump.class,
                CommandLineHandlerIdentityStoreInitDemo.class
        }
)
public class CommandLineHandlerIdentityStore {
  public static final String DEFAULT_FILENAME = "identityStore.cfg";

}
