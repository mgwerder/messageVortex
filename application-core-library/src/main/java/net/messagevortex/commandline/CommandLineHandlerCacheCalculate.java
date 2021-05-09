package net.messagevortex.commandline;

import net.messagevortex.asn1.AsymmetricKey;
import picocli.CommandLine;
import sun.misc.Signal;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * <p>Commandline handler for pre-populating keys in cache.</p>
 */
@CommandLine.Command(
        description = "Add keys to cache",
        name = "cache2",
        aliases = {"cache2"},
        mixinStandardHelpOptions = true
)
public class CommandLineHandlerCacheCalculate implements Callable<Integer> {
  
  /**
   * duration for the pre-calculation to take place. Use -1 (default) for "no time limit"
   **/
  @CommandLine.Option(names = {"--seconds"},
          description = "number of seconds to run the calculator (-1) for infinite")
  private int seconds = -1;
  
  /**
   * <p>Run a cache pre-calculator to fill cache for the specified duration.</p>
   *
   * @return always returns exit code 0
   * @throws IOException when unable to run the cache pre-calculator
   */
  public Integer call() throws IOException {
    
    // just create an instance and wait for the Cache to fill
    new AsymmetricKey();
    
    // install signal handler for HUP to abort pre-calculation
    Signal.handle(new Signal("HUP"), signal -> {
      AsymmetricKey.setCacheFileName(null);
    });
    
    int i = 0;
    while ((i < seconds || i == -1) && AsymmetricKey.getCacheFileName() != null) {
      try {
        Thread.sleep(1000);
        i++;
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    }
    AsymmetricKey.setCacheFileName(null);
    
    return 0;
  }
}
