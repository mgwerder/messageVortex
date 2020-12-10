package net.messagevortex.commandline;

import java.util.concurrent.Callable;
import net.messagevortex.NotImplementedException;
import picocli.CommandLine;

/**
 * Commandline handler for generating asymmetric keys.
 *
 * <p>This class serves as commandline command rump only.</p>
 */
@CommandLine.Command(
        description = "Helper for cache of asymmetric keys",
        name = "cache",
        mixinStandardHelpOptions = true,
        subcommands = {
                CommandLineHandlerCacheCalculate.class,
        }
)
public class CommandLineHandlerCache {};
