package net.messagevortex.commandline;

import java.util.concurrent.Callable;
import net.messagevortex.NotImplementedException;
import picocli.CommandLine;

@CommandLine.Command(
    description = "Helper for add redundancy operations",
    name = "redundancy",
    aliases = {"red"},
    mixinStandardHelpOptions = true,
    subcommands = {
        CommandLineHandlerRedundancyAdd.class
    }
)
public class CommandLineHandlerRedundancy implements Callable<Integer> {

  public Integer call() throws Exception {
    throw new NotImplementedException();
  }

}
