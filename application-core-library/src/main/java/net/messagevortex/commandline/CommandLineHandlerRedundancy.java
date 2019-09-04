package net.messagevortex.commandline;

import net.messagevortex.NotImplementedException;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        description = "Helper for add redundancy operations",
        name = "redundancy",
        aliases = { "red" },
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
