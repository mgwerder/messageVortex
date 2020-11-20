package net.messagevortex.commandline;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.AsymmetricKey;
import net.messagevortex.asn1.AsymmetricKeyCache;
import net.messagevortex.asn1.encryption.Algorithm;
import picocli.CommandLine;
import sun.misc.Signal;

@CommandLine.Command(
    description = "Add key to cache",
    name = "cache",
    aliases = {"cache"},
    mixinStandardHelpOptions = true
)
public class CommandLineHandlerCacheCalculate implements Callable<Integer> {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @CommandLine.Option(names = {"--seconds"},
      description = "number of seconds to run the calculator (-1) for infinite")
  private int seconds = -1;

  public Integer call() throws Exception {

    // just create an instance and wait for the Cache to fill
    final AsymmetricKey a = new AsymmetricKey();

    Signal.handle(new Signal("HUP"), signal -> {
      AsymmetricKey.setCacheFileName(null);
    });

    int i=0;
    while(i<seconds || i==-1) {
      try {
        Thread.sleep(1000);
        i++;
      }catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    }

    return 0;
  }
}
