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
import net.messagevortex.blender.Blender;
import net.messagevortex.router.Router;
import net.messagevortex.transport.Transport;
import picocli.CommandLine;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@CommandLine.Command(description = "A MessageVortex implementation for the privacy aware person.",
        name = "MessageVortex", mixinStandardHelpOptions = true, versionProvider = Version.class )
public class MessageVortex implements Callable<Integer> {

  private static final Logger LOGGER;

  private final static int CONFIG_FAIL   = 101;
  private final static int SETUP_FAIL    = 102;
  private final static int ARGUMENT_FAIL = 103;

  private enum DaemonType {
    TRANSPORT,
    BLEDING,
    ROUTING,
    ACCOUNTING;
  }

  @CommandLine.Option(names = {"-c", "--config"}, description = "filename of the config to be used")
  private String configFile = "messageVortex.cfg";

  @CommandLine.Option(names = {"--timeoutAndDie"}, hidden = true, description = "timeout before aboorting execution (for test purposes only)")
  private int timeoutInSeconds = -1;

  static {
    LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private static Map<String,Transport>  transport  = new ConcurrentHashMap<>();
  private static Map<String,Blender>    blender    = new ConcurrentHashMap<>();
  private static Map<String,Router>     router     = new ConcurrentHashMap<>();
  private static Map<String,Accountant> accountant = new ConcurrentHashMap<>();

  public static void main(String[] args) {
    int retval = mainReturn(args);
    if (retval>0) {
      System.exit(retval);
    }
  }

  public static int mainReturn(String[] args) {
    LOGGER.log(Level.INFO, "MessageVortex V" + Version.getBuild());
    Integer i = CommandLine.call(new MessageVortex(), args == null ? new String[0] : args);
    return i != null ? i : ARGUMENT_FAIL;
  }

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
      // setup according to config file
      Config cfg = MessageVortexConfig.getDefault();
      for (String routerSection : cfg.getSectionListValue(null,"router_setup")) {
        LOGGER.log(Level.INFO, "setting up routing layer \"" + routerSection + "\"");
        // setup Accounting
        accountant.put(routerSection.toLowerCase(), (Accountant)getDaemon(routerSection,cfg.getStringValue(routerSection, "accounting_implementation"),DaemonType.ACCOUNTING));
        // setup routers
        router.put(routerSection.toLowerCase(), (Router)getDaemon(routerSection,cfg.getStringValue(routerSection, "router_implementation"),DaemonType.ROUTING));
      }
      for (String blendingSection : cfg.getSectionListValue(null,"blender_setup")) {
        LOGGER.log(Level.INFO, "setting up blending layer \"" + blendingSection + "\"");
        // setup blending
        blender.put(blendingSection.toLowerCase(), (Blender)getDaemon(blendingSection,cfg.getStringValue(blendingSection, "blender_implementation"),DaemonType.BLEDING));
      }
      for (String transportSection : cfg.getSectionListValue(null,"transport_setup")) {
        LOGGER.log(Level.INFO, "setting up transport layer \"" + transportSection + "\"");
        // setup transport
        transport.put(transportSection.toLowerCase(), (Transport)getDaemon(transportSection,cfg.getStringValue(transportSection, "transport_implementation"),DaemonType.TRANSPORT));
      }

    } catch (IOException ioe) {
      LOGGER.log(Level.SEVERE, "Exception while setting up infrastructure", ioe);
      return SETUP_FAIL;
    } catch(ClassNotFoundException cnf) {
      LOGGER.log(Level.SEVERE, "Bad class configured", cnf);
      return SETUP_FAIL;
    }
    LOGGER.log(Level.INFO, "******* startup of MessageVortex complete *******");


    if (timeoutInSeconds>=0) {
      try {
        Thread.sleep(timeoutInSeconds * 1000);
      } catch(InterruptedException ie) {
        // may be safely ignored
      }
    } else {
      MessageVortexController controller = new MessageVortexController();
      controller.waitForShutdown();
    }

    LOGGER.log(Level.INFO, "******* shutting down MessageVortex *******");
    Map<String,RunningDaemon> tmap = new HashMap<>();
    tmap.putAll(transport);
    tmap.putAll(blender);
    tmap.putAll(router);
    tmap.putAll(accountant);
    for (Map.Entry<String,RunningDaemon> es:tmap.entrySet() ) {
      LOGGER.log(Level.INFO,"shutting down "+es.getKey());
      es.getValue().shutdownDaemon();
    }
    LOGGER.log(Level.INFO, "******* shutdown complete *******");
    return 0;
  }

  /**
   * <p>This is a wraper of the getConfiguredClass() methode.</p>
   *
   * <p>it modifyes the return type to a RunningDaemon.</p>
   *
   * @param section
   * @param classname
   * @param type
   * @return
   * @throws ClassNotFoundException
   */
  public static RunningDaemon getDaemon(String section, String classname,DaemonType type) throws ClassNotFoundException {
    return (RunningDaemon)getConfiguredClass(section, classname, RunningDaemon.class);
  }

  /**
   * <p>Loads a class of the given type.</p>
   *
   * <p>The class must have the same or a subtype of the template class and must provide a String
   * constructor, taking the name of the config section.</p>
   *
   * @param section         the configuration section where the class was mentioned
   *                        (for information purposes in output and to load subsequent data)
   * @param name            the Name of the class
   * @param templateClass   a template class. The loaded class must be the same or a subtyppe of
   *                        the template class
   * @return                the loaded class
   * @throws ClassNotFoundException
   */
  public static Object getConfiguredClass(String section, String name, Class templateClass) throws ClassNotFoundException {
    if (name == null) {
      throw new ClassNotFoundException("unable to obtain class \"null\"");
    }
    Class<RunningDaemon> myClass = (Class<RunningDaemon>)(Class.forName(name));
    Constructor<?> myConstructor;
    try {
      myConstructor = myClass.getConstructor(String.class);
    } catch(NoSuchMethodException e) {
      throw new ClassNotFoundException( "unable to get apropriate constructor from class \""+name+"\"", e);
    }
    if (! templateClass.isAssignableFrom(myClass)) {
      throw new ClassNotFoundException( "Class \"" + name + "\" does not implement required interfaces");
    }
    try {
      return myConstructor.newInstance(new Object[]{section});
    } catch(IllegalAccessException|InvocationTargetException|InstantiationException e) {
      throw new ClassNotFoundException( "Class \"" + name + "\" failed running the constructor",e);
    }
  }

  public static Accountant getAccountant(String id) {
    return accountant.get(id.toLowerCase());
  }

  public static Blender getBlender(String id) {
    return blender.get(id.toLowerCase());
  }

  public static Router getRouter(String id) {
    return router.get(id.toLowerCase());
  }
}
