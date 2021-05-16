package net.messagevortex;

// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// ************************************************************************************

import net.messagevortex.accounting.Accountant;
import net.messagevortex.asn1.AsymmetricKeyPreCalculator;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.blender.Blender;
import net.messagevortex.blender.recipes.BlenderRecipe;
import net.messagevortex.commandline.CommandLineHandlerCipher;
import net.messagevortex.commandline.CommandLineHandlerExamples;
import net.messagevortex.commandline.CommandLineHandlerIdentityStore;
import net.messagevortex.commandline.CommandLineHandlerInit;
import net.messagevortex.commandline.CommandLineHandlerRedundancy;
import net.messagevortex.commandline.CommandLineHandlerVersion;
import net.messagevortex.router.Router;
import net.messagevortex.transport.Transport;
import net.messagevortex.transport.dummy.DummyTransportTrx;
import picocli.CommandLine;

import javax.crypto.Cipher;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandLine.Command(
    description = "A MessageVortex implementation for the privacy aware person.",
    name = "MessageVortex",
    mixinStandardHelpOptions = true,
    versionProvider = Version.class,
    subcommands = {
        AsymmetricKeyPreCalculator.class,
        CommandLineHandlerIdentityStore.class,
        CommandLineHandlerCipher.class,
        CommandLineHandlerVersion.class,
        CommandLineHandlerInit.class,
        CommandLineHandlerRedundancy.class,
        CommandLineHandlerExamples.class
    }
)
public class MessageVortex implements Callable<Integer> {

  public static final int CONFIG_FAIL = 101;
  public static final int SETUP_FAIL = 102;
  public static final int ARGUMENT_FAIL = 103;

  private static final Logger LOGGER;

  private enum DaemonType {
    TRANSPORT,
    BLEDING,
    ROUTING,
    ACCOUNTING,
    IDENTITY_STORE
  }

  @CommandLine.Option(names = {"-c", "--config"}, description = "filename of the config to be used")
  private String configFile = "messageVortex.cfg";

  @CommandLine.Option(names = {"--timeoutAndDie"}, hidden = true,
      description = "timeout before aboorting execution (for test purposes only)")
  private int timeoutInSeconds = -1;

  @CommandLine.Option(names = {"--threadDumpInteval"}, hidden = true,
      description = "timeout before aboorting execution (for test purposes only)")
  private int threadDumpInterval = 300;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private static Integer JRE_AES_KEY_SIZE = null;

  private boolean verifyPrerequisites() {
    LOGGER.log(Level.INFO, "Checking bouncycastle version...");
    String bcversion = org.bouncycastle.jce.provider.BouncyCastleProvider
        .class
        .getPackage()
        .getImplementationVersion();
    LOGGER.log(Level.INFO, "Detected BouncyCastle version is " + bcversion);
    if (bcversion == null) {
      LOGGER.log(Level.SEVERE, "unable to determine BC version (got NULL value)");
      return false;
    }
    Matcher m = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(beta(\\d*))?")
        .matcher(bcversion);
    if (!m.matches()) {
      LOGGER.log(Level.SEVERE, "unable to parse BC version (" + bcversion + ")");
      return false;
    } else {
      int major = Integer.parseInt(m.group(1));
      int minor = Integer.parseInt(m.group(2));
      if ((major == 1 && minor >= 60) || (major >= 2)) {
        LOGGER.log(Level.INFO, "Detected BC version is " + bcversion
            + ". This should do the trick.");
      } else {
        LOGGER.log(Level.SEVERE, "Looks like your BC installation is heavily outdated. "
            + "At least version 1.60 is recommended.");
        return false;
      }
    }

    LOGGER.log(Level.INFO, "Checking JRE");
    try {
      String jreversion = System.getProperty("java.version");
      LOGGER.log(Level.INFO, "JRE  version is " + jreversion);
      if (JRE_AES_KEY_SIZE == null) {
        JRE_AES_KEY_SIZE = Cipher.getMaxAllowedKeyLength("AES");
      }
      int i = JRE_AES_KEY_SIZE;
      if (i > 128) {
        LOGGER.log(Level.INFO, "Looks like JRE having an unlimited JCE installed (AES max allowed "
            + "key length is = " + i + "). This is good.");
      } else {
        LOGGER.log(Level.SEVERE, "Looks like JRE not having an unlimited JCE installed (AES max "
            + "allowed key length is = " + i + "). This is bad.");
        return false;
      }
    } catch (NoSuchAlgorithmException nsa) {
      LOGGER.log(Level.SEVERE, "OOPS... Got an exception while testing for an unlimited JCE. "
          + "This is bad.", nsa);
      return false;
    }
    return true;
  }

  /***
   * <p>Main command line method.</p>
   *
   * @param args command line parameters
   */
  public static void main(String[] args) {
    int retval = mainReturn(args);
    if (retval > 0) {
      System.exit(retval);
    }
  }

  /***
   * <p>Wrapper function as entry point for tests.</p>
   *
   * @param args command line arguments
   * @return the errorlevel to be returned
   */
  public static int mainReturn(String[] args) {
    LOGGER.log(Level.INFO, "MessageVortex V" + Version.getBuild());
    CommandLine c = new CommandLine(new MessageVortex());
    c.execute(args == null ? new String[0] : args);
    Integer i = c.getExecutionResult();
    return i != null ? i : ARGUMENT_FAIL;
  }

  @Override
  public Integer call() {
    LOGGER.log(Level.INFO, "******* startup of MessageVortex *******");
    // create config store
    try {
      LOGGER.log(Level.INFO, "Loading config file");
      MessageVortexConfig.getDefault().load(configFile);
    } catch (IOException ioe) {
      LOGGER.log(Level.SEVERE, "Unable to parse config file", ioe);
      return CONFIG_FAIL;
    }

    try {
      // check prerequisites
      verifyPrerequisites();

      Config cfg = MessageVortexConfig.getDefault();

      // load IdentityStore
      MessageVortexRepository.setIdentityStore("", "default_identity_store", new IdentityStore(
          new File(CommandLineHandlerIdentityStore.DEFAULT_FILENAME)));

      // setup non-standard identity stores
      for (String idstoreSection : cfg.getSectionListValue(null, "identity_store_setup")) {
        LOGGER.log(Level.INFO, "setting up identity store \"" + idstoreSection
            + "\"");
        String fn = cfg.getStringValue(idstoreSection, "filename");
        if (fn == null) {
          throw new IOException("unable to obtain identity store filename of section "
              + idstoreSection + "");
        }
        File f = new File(fn);
        if (!f.exists()) {
          throw new IOException("identity store file \"" + fn + "\" not found");
        }
        MessageVortexRepository.setIdentityStore("", idstoreSection.toLowerCase(), new IdentityStore(f));
      }

      // setup recipes
      // create default recipe store
      String lst = Config.getDefault().getStringValue(null, "recipes");
      BlenderRecipe.clearRecipes(null);
      for (String cl : lst.split(" *, *")) {
        BlenderRecipe.addRecipe(null,
            (BlenderRecipe) getConfiguredClass(null, cl, BlenderRecipe.class));
      }
      for (String accountingSection : cfg.getSectionListValue(null, "recipe_setup")) {
        // FIXME there is something missing here! Just found this unused code snipped...
        // I really need some sleep.
      }

      //Setup accounting
      for (String accountingSection : cfg.getSectionListValue(null, "accountant_setup")) {

        // setup Accounting
        LOGGER.log(Level.INFO, "setting up accounting for routing layer \"" + accountingSection
            + "\"");
        MessageVortexRepository.setAccountant("", accountingSection.toLowerCase(), (Accountant) getDaemon(accountingSection,
            cfg.getStringValue(accountingSection, "accounting_implementation"),
            DaemonType.ACCOUNTING));

      }

      // setup routers
      for (String routerSection : cfg.getSectionListValue(null, "router_setup")) {
        // setup routers
        LOGGER.log(Level.INFO, "setting up routing layer \"" + routerSection + "\"");
        MessageVortexRepository.setRouter("", routerSection.toLowerCase(),
            (Router) getDaemon(routerSection, cfg.getStringValue(routerSection,
                "router_implementation"), DaemonType.ROUTING));
      }

      // setup blending
      for (String blendingSection : cfg.getSectionListValue(null, "blender_setup")) {
        // setup blending
        LOGGER.log(Level.INFO, "setting up blending layer \"" + blendingSection + "\"");
        MessageVortexRepository.setBlender("", blendingSection.toLowerCase(),
            (Blender) getDaemon(blendingSection, cfg.getStringValue(blendingSection,
                "blender_implementation"), DaemonType.BLEDING));

      }

      // Setup transport
      for (String transportSection : cfg.getSectionListValue(null, "transport_setup")) {
        // setup transport
        LOGGER.log(Level.INFO, "setting up transport layer \"" + transportSection + "\"");
        MessageVortexRepository.setTransport("", transportSection.toLowerCase(),
            (Transport) getDaemon(transportSection, cfg.getStringValue(transportSection,
                "transport_implementation"), DaemonType.TRANSPORT));
        LOGGER.log(Level.INFO, "  setting up of \"" + transportSection + "\" is done");
      }
    } catch (IOException ioe) {
      LOGGER.log(Level.SEVERE, "Exception while setting up infrastructure", ioe);
      return SETUP_FAIL;
    } catch (ClassNotFoundException cnf) {
      LOGGER.log(Level.SEVERE, "Bad class configured", cnf);
      return SETUP_FAIL;
    }

    // enable thread dumper
    if (threadDumpInterval > 0) {
      LOGGER.log(Level.INFO, "starting thread dumper with interval " + threadDumpInterval);
      new ThreadDumper(threadDumpInterval);
    }

    LOGGER.log(Level.INFO, "******* startup of MessageVortex complete *******");

    MessageVortexController controller = new MessageVortexController();
    controller.setTimeout(timeoutInSeconds * 1000);
    controller.waitForShutdown();

    LOGGER.log(Level.INFO, "******* shutting down MessageVortex *******");
    Map<String, RunningDaemon> tmap = MessageVortexRepository.getRunningDaemons("");
    for (Map.Entry<String, RunningDaemon> es : tmap.entrySet()) {
      LOGGER.log(Level.INFO, "shutting down " + es.getKey());
      es.getValue().shutdownDaemon();
    }

    MessageVortexRepository.clear("");

    // remove all entries from identity store
    DummyTransportTrx.clearDummyEndpoints();
    LOGGER.log(Level.INFO, "******* shutdown complete *******");
    return 0;
  }

  /**
   * <p>This is a wrapper of the getConfiguredClass() methode.</p>
   *
   * <p>It modifies the return type to a RunningDaemon.</p>
   *
   * @param section   the name of the section where the config should be taken from
   * @param classname the name of the class to be instantiated
   * @param type      the type of daemon to be checked
   * @return the specified object
   * @throws ClassNotFoundException if classname not found
   **/
  public static RunningDaemon getDaemon(String section, String classname, DaemonType type)
      throws ClassNotFoundException {
    return (RunningDaemon) getConfiguredClass(section, classname, RunningDaemon.class);
  }

  /**
   * <p>Loads a class of the given type.</p>
   *
   * <p>The class must have the same or a subtype of the template class and must provide a String
   * constructor, taking the name of the config section.</p>
   *
   * @param section       the configuration section where the class was mentioned
   *                      (for information purposes in output and to load subsequent data)
   * @param name          the Name of the class
   * @param templateClass a template class. The loaded class must be the same or a subtyppe of
   *                      the template class
   * @return the loaded class
   * @throws ClassNotFoundException if the named class cannot be found
   **/
  public static Object getConfiguredClass(String section, String name, Class templateClass)
      throws ClassNotFoundException {
    if (name == null) {
      throw new ClassNotFoundException("unable to obtain class \"null\"");
    }
    @SuppressWarnings("unchecked")
    Class<RunningDaemon> myClass = (Class<RunningDaemon>) (Class.forName(name));
    Constructor<?> myConstructor;
    try {
      myConstructor = myClass.getConstructor(String.class);
    } catch (NoSuchMethodException e) {
      throw new ClassNotFoundException("unable to get apropriate constructor from class \""
          + name + "\"", e);
    }
    if (!templateClass.isAssignableFrom(myClass)) {
      throw new ClassNotFoundException("Class \"" + name
          + "\" does not implement required interfaces");
    }
    try {
      return myConstructor.newInstance(section);
    } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
      throw new ClassNotFoundException("Class \"" + name + "\" failed running the constructor", e);
    }
  }

}
