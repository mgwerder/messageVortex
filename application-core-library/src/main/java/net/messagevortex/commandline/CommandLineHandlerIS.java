package net.messagevortex.commandline;

import java.util.concurrent.Callable;
import net.messagevortex.NotImplementedException;
import picocli.CommandLine;

@CommandLine.Command(
        description = "Manipulator for IdentityStore",
        name = "intentitystore",
        aliases = { "store","is" },
        mixinStandardHelpOptions = true,
        subcommands = {
                CommandLineHandlerISCreate.class,
                CommandLineHandlerISDump.class
        }
)
public class CommandLineHandlerIS implements Callable<Integer> {
  public static final String DEFAULT_FILENAME = "identityStore.cfg";

  public Integer call() throws Exception {
    throw new NotImplementedException();
  }
}
