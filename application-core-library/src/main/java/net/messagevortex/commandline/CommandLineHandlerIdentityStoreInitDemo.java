package net.messagevortex.commandline;

import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.encryption.DumpType;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;

/**
 * <p>Commandline handler to generate new demo identities.</p>
 */
@CommandLine.Command(
        description = "generate a new set of demo identity stores",
        name = "initDemo",
        aliases = {"init"},
        mixinStandardHelpOptions = true
)
public class CommandLineHandlerIdentityStoreInitDemo implements Callable<Integer> {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  /**
   * <p>Commandline handler to create a demo identity store.</p>
   *
   * <p>Use java -jar MessageVortex.jar identitystore initDemo --help to get all supported
   * parameters.</p>
   *
   * @return the error level to be returned
   * @throws Exception if anything goes wrong
   */
  @Override
  public Integer call() throws Exception {
    // check store
    if (new File(CommandLineHandlerIdentityStore.DEFAULT_FILENAME).exists()) {
      LOGGER.log(Level.SEVERE, "File \"" + CommandLineHandlerIdentityStore.DEFAULT_FILENAME
              + "\" exists already");
      return MessageVortex.ARGUMENT_FAIL;
    }

    // create store
    LOGGER.log(Level.INFO, "creating identity store \""
            + CommandLineHandlerIdentityStore.DEFAULT_FILENAME + "\"");
    int ret = MessageVortex.mainReturn(new String[]{"store", "create"});
    if (ret != 0) {
      throw new IOException("Error while creating a new store");
    }

    // creating demo identities
    for (int i = 1; i <= 9; i++) {

      // delete old identity store
      File f = new File("is_dummy" + i + ".cfg");
      if (f.exists() && f.delete()) {
        LOGGER.log(Level.INFO, "old identity store \"" + f.getName() + "\" deleted");
      }

      // generate new store
      LOGGER.log(Level.INFO, "creating identity store \"" + f.getName() + "\"");
      ret = MessageVortex.mainReturn(new String[]{"store", "create", "--filename", f.getName()});
      if (ret != 0) {
        throw new IOException("Error while creating a new store");
      }

      // generate new identity
      String tid = "Dummy" + i + "@localhost";
      LOGGER.log(Level.INFO, "creating new identity " + tid + " in \"" + f.getName() + "\"");
      ret = MessageVortex.mainReturn(new String[]{"store", "generate", "--filename",
              f.getName(), "--identityName", tid});
      if (ret != 0) {
        throw new IOException("Error while creating identity store");
      }

      // load new identity store
      IdentityStore ist = new IdentityStore(f);
      LOGGER.log(Level.INFO, "new identity " + tid + " is " + ist.getIdentityList()[0]);
      String newUrl = ist.getIdentity(ist.getIdentityList()[0]).getUrl();
      LOGGER.log(Level.INFO, "extracted new identity " + tid + " from \"" + f.getName() + "\" ("
              + newUrl + ")");

      // Add identity to the main identity store
      LOGGER.log(Level.INFO, "adding new identity for " + tid + " to main store");
      ret = MessageVortex.mainReturn(new String[]{"store", "add", "--identity", newUrl});
      if (ret != 0) {
        throw new IOException("Error while creating a new store");
      }
    }

    // dump store
    LOGGER.log(Level.INFO, "Dumping identity store");
    IdentityStore is = new IdentityStore(
            new File(CommandLineHandlerIdentityStore.DEFAULT_FILENAME));
    System.out.println(is.dumpValueNotation("", DumpType.ALL_UNENCRYPTED));

    LOGGER.log(Level.INFO, "finished");
    return 0;
  }
}

