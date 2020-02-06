package net.messagevortex.commandline;

import java.util.concurrent.Callable;
import net.messagevortex.NotImplementedException;
import picocli.CommandLine;

@CommandLine.Command(
        description = "Helper for cache of asymmetric keys",
        name = "cache",
        mixinStandardHelpOptions = true,
        subcommands = {
                CommandLineHandlerCacheCalculate.class,
        }
)
public class CommandLineHandlerCache implements Callable<Integer> {

  public Integer call() throws Exception {
    throw new NotImplementedException();
  }

}
