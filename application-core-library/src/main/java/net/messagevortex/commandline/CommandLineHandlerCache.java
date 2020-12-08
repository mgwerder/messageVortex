package net.messagevortex.commandline;

import java.util.concurrent.Callable;
import net.messagevortex.NotImplementedException;
import picocli.CommandLine;

/**
 * Commandline handler for generating asymmetric keys.
 */
@CommandLine.Command(
        description = "Helper for cache of asymmetric keys",
        name = "cache",
        mixinStandardHelpOptions = true,
        subcommands = {
                CommandLineHandlerCacheCalculate.class,
        }
)
public class CommandLineHandlerCache implements Callable<Integer> {
  
  /**
   * <p>Dummy handler only providing sub calls.</p>
   * @TODO not implemented
   * @return nothing
   * @throws Exception always
   */
  public Integer call() throws Exception {
    throw new NotImplementedException();
  }

}
