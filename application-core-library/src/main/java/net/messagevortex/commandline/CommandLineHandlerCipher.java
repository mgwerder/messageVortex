package net.messagevortex.commandline;

import java.util.concurrent.Callable;
import net.messagevortex.NotImplementedException;
import picocli.CommandLine;

@CommandLine.Command(
        description = "Helper for symmetrical encryption operations",
        name = "cipher",
        aliases = { "cip","cypher","cyp" },
        mixinStandardHelpOptions = true,
        subcommands = {
                CommandLineHandlerCipherList.class,
                CommandLineHandlerCipherEncrypt.class
        }
)
public class CommandLineHandlerCipher implements Callable<Integer> {

  public Integer call() throws Exception {
    throw new NotImplementedException();
  }

}
