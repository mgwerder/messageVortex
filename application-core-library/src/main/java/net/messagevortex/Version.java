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

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import picocli.CommandLine;

public class Version implements CommandLine.IVersionProvider {

  private static final Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private static String intVersion = null;

  private static int MAJOR = -1; //@major@
  private static int MINOR = -1; //@minor@
  private static int REVISION = -1; //@revision@
  private static final String GIT_BUILD = "$Id$";
  private static final String BUILD = GIT_BUILD.substring(5, GIT_BUILD.length() - 2);

  private static String VERSION_STRING = null;
  private static String BUILDVER = null;

  private Version() {
    super();
  }

  public static String getBuild() {
    init();
    return BUILDVER;
  }

  public static String getStringVersion() {
    init();
    return intVersion;
  }

  public String[] getVersion() {
    init();
    return new String[] { VERSION_STRING };
  }

  private static synchronized void init() {
    if (intVersion == null) {
      String version = null;

      // load from properties first
      try (InputStream is = Config.class.getResourceAsStream(
                      "/META-INF/messageVortex.properties");) {
        Properties p = new Properties();
        if (is != null) {
          p.load(is);
          version = p.getProperty("application.version", "");
        }
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "unable to get version number from property file", e);
      }
      // fallback to using package API
      if (version == null) {
        Package pkg = Config.class.getPackage();
        if (pkg != null) {
          version = pkg.getImplementationVersion();
          if (version == null) {
            version = pkg.getSpecificationVersion();
          }
        }
      }

      if (version != null) {
        intVersion = version;
        Pattern versionPat = Pattern.compile("^\\s*([0-9]+)\\.([0-9]+)\\.([0-9]+)\\s*$");
        Matcher m = versionPat.matcher(intVersion);
        if (m.matches()) {
          MAJOR    = Integer.parseInt(m.group(1));
          MINOR    = Integer.parseInt(m.group(2));
          REVISION = Integer.parseInt(m.group(3));
          VERSION_STRING = MAJOR + "." + MINOR + "." + REVISION;
          BUILDVER = VERSION_STRING + " (" + BUILD + ")";
        } else {
          LOGGER.log(Level.SEVERE, "Version " + intVersion
                  + " does not match the required regular expression");
          LOGGER.log(Level.SEVERE, "If this happens while testing in IDE try to"
                  + " run 'mvn package'.");
        }
      } else {
        LOGGER.log(Level.SEVERE, "unable to get version number of application");
      }
    }
  }

}
