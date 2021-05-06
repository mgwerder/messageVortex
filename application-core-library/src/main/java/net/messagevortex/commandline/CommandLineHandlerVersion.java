package net.messagevortex.commandline;

import net.messagevortex.Version;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
    description = "Get detailed version information",
    name = "version",
    aliases = {"v", "ver"},
    mixinStandardHelpOptions = true,
    subcommands = {
        CommandLineHandlerCacheCalculate.class,
    }
)
public class CommandLineHandlerVersion implements Callable<Integer> {

  /***
   * <p>Commandline handler to display application version.</p>
   *
   * @return the error level
   */
  @Override
  public Integer call() {
    System.out.println("VERSION=" + Version.getStringVersion());
    System.out.println("BUILD=" + Version.getBuild());
    return 0;
  }

}
