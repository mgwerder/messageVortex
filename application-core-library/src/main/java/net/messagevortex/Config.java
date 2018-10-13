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

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class Config {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private enum ConfigType {
    BOOLEAN,
    NUMERIC,
    STRING;

    public static ConfigType getById(String id) {
      for (ConfigType c : values()) {
        if (c.name().toLowerCase().equals(id.toLowerCase())) {
          return c;
        }
      }
      return null;
    }
  }

  private class ConfigElement implements Comparator<ConfigElement> {

    private String id;
    private String type;
    private String description;
    private String defaultValue;
    private String currentValue;

    public ConfigElement(String id, String type) {
      setId(id);
      setType(type);
      setDefaultValue(null);
      setDescription(null);
    }

    public ConfigElement(String id, String type, String description) {
      this(id, type);
      setDescription(description);
    }

    public ConfigElement copy() {
      ConfigElement ret = new ConfigElement(id, type, description);
      setDefaultValue(defaultValue);
      setValue(currentValue);
      return ret;
    }

    public final void setId(String id) {
      if (id == null) {
        throw new NullPointerException("id must not be null");
      }
      this.id = id.toLowerCase();
    }

    public final void setType(String type) {
      if (type == null) {
        throw new NullPointerException("type must not be null");
      } else if (ConfigType.getById(type) == null) {
        throw new IllegalArgumentException("type " + type + " is not a known config type");
      } else {
        this.type = type.toLowerCase();
      }
    }

    public final ConfigType getType() {
      return ConfigType.getById(this.type);
    }

    public final void setDescription(String description) {
      this.description = description;
    }

    public final String getStringValue() {
      if (currentValue != null) {
        return currentValue;
      } else {
        return defaultValue;
      }
    }

    public final String setStringValue(String value) {
      String ret = getStringValue();
      currentValue = value;
      return ret;
    }

    public final boolean getBooleanValue() {
      String ret = getStringValue();
      return ret != null && ("true".equals(ret.toLowerCase()) || "yes".equals(ret.toLowerCase()));
    }

    public final boolean setBooleanValue(boolean value) {
      boolean ret = getBooleanValue();
      if (value) {
        currentValue = "true";
      } else {
        currentValue = "false";
      }
      return ret;
    }

    public final int getNumericValue() {
      return Integer.parseInt(getStringValue());
    }

    public final int setNumericValue(int value) {
      int ret = getNumericValue();
      setValue("" + value);
      return ret;
    }

    public final String unset() {
      return setValue(null);
    }

    private String setValue(String value) {
      String ret = getStringValue();
      if (defaultValue != null && defaultValue.equals(value)) {
        ret = unset();
      } else {
        this.currentValue = value;
      }
      return ret;
    }

    @Override
    public int compare(ConfigElement o1, ConfigElement o2) {
      return o1.id.compareToIgnoreCase(o2.id);
    }

    public final void setDefaultValue(String defaultValue) {
      this.defaultValue = defaultValue;
    }

  }

  static Config defaultConfig = null;
  private final Map<String, ConfigElement> configurationData = new ConcurrentHashMap<>();

  public static Config getDefault() {
    return defaultConfig;
  }

  /***
   * <p>Returns a deep copy of this config store.</p>
   *
   * @return the copy
   */
  public Config copy() throws IOException {
    Config dst = createConfig();
    synchronized (configurationData) {
      Set<Map.Entry<String, ConfigElement>> it = configurationData.entrySet();
      for (Map.Entry<String, ConfigElement> p : it) {
        dst.configurationData.put(p.getKey(), p.getValue().copy());
      }
    }
    return dst;
  }

  /***
   * <p>Creates a new boolean config value in the store.</p>
   *
   * @param id           the name (id) of the new value
   * @param description  the description for the value
   * @param dval         the default value
   */
  public void createBooleanConfigValue(String id, String description, boolean dval) {
    synchronized (configurationData) {
      if (configurationData.get(id.toLowerCase()) == null) {
        ConfigElement ele = new ConfigElement(id, "boolean", description);
        configurationData.put(id.toLowerCase(), ele);
        ele.setDefaultValue(dval ? "true" : "false");
        LOGGER.log(Level.FINE, "Created boolean config variable " + id.toLowerCase());
      } else {
        throw new IllegalArgumentException("id \"" + id + "\" is already defined");
      }
    }
  }

  public static Config createConfig() throws IOException {
    return new Config();
  }

  /***
   * <p>Sets a boolean value in the application config.</p>
   *
   * @param id key which should be set
   * @param value Vlue to be set in key
   * @return old value before setting to new value
   *
   * @throws NullPointerException if key does not exist in configurationData
   * @throws ClassCastException if key is not of type boolean
   */
  public boolean setBooleanValue(String id, boolean value) {
    ConfigElement ele = configurationData.get(id.toLowerCase());
    if (ele == null) {
      throw new NullPointerException("id " + id + " is not known to the config subsystem");
    }
    ConfigType type = ele.getType();
    if (type != ConfigType.BOOLEAN) {
      throw new ClassCastException("config type missmatch when accessing ID " + id
              + " (expected: boolean; is: " + type.name() + ")");
    }
    return ele.setBooleanValue(value);
  }

  /***
   * <p>Gets a boolean value from the application config.</p>
   *
   * @param id          key which should be set
   * @return current value of the specified key
   * @throws NullPointerException if key does not exist in configurationData
   * @throws ClassCastException if key is not of type boolean
   */
  public boolean getBooleanValue(String id) {
    ConfigElement ele = configurationData.get(id.toLowerCase());
    if (ele == null) {
      throw new NullPointerException("id " + id + " is not known to the config subsystem");
    }
    ConfigType type = ele.getType();
    if (type != ConfigType.BOOLEAN) {
      throw new ClassCastException("config type missmatch when accessing ID " + id
              + " (expected: boolean; is: " + type.name() + ")");
    }
    return ele.getBooleanValue();
  }

  /***
   * <p>Creates a new numeric config value in the store.</p>
   *
   * @param id           the name (id) of the new value
   * @param description  the description for the value
   * @param dval         the default value
   */
  public void createNumericConfigValue(String id, String description, int dval) {
    synchronized (configurationData) {
      if (configurationData.get(id.toLowerCase()) == null) {
        ConfigElement ele = new ConfigElement(id, "numeric", description);
        configurationData.put(id.toLowerCase(), ele);
        ele.setDefaultValue("" + dval);
        LOGGER.log(Level.FINE, "Created numeric config variable " + id.toLowerCase());
      } else {
        throw new IllegalArgumentException("id \"" + id + "\" is already defined");
      }
    }
  }

  /***
   * <p>Sets a numeric value in the application config.</p>
   *
   * @param id key which should be set
   * @param value Value to be set in key
   * @return old value before setting to new value
   * @throws NullPointerException if key does not exist in configurationData
   * @throws ClassCastException if key is not of type boolean
   */
  public int setNumericValue(String id, int value) throws IOException {
    ConfigElement ele = configurationData.get(id.toLowerCase());
    if (ele == null) {
      throw new NullPointerException("id " + id + " is not known to the config subsystem");
    }
    ConfigType type = ele.getType();
    if (type != ConfigType.NUMERIC) {
      throw new ClassCastException("config type missmatch when accessing ID " + id
              + " (expected: numeric; is: " + type.name() + ")");
    }
    return ele.setNumericValue(value);
  }

  /***
   * <p>Gets a numeric value from the application config.</p>
   *
   * @param id          key which should be set
   * @return current value of the specified key
   * @throws NullPointerException if key does not exist in configurationData
   * @throws ClassCastException if key is not of type boolean
   */
  public int getNumericValue(String id) throws IOException {
    ConfigElement ele = configurationData.get(id.toLowerCase());
    if (ele == null) {
      throw new NullPointerException("id " + id + " is not known to the config subsystem");
    }
    ConfigType type = ele.getType();
    if (type != ConfigType.NUMERIC) {
      throw new ClassCastException("config type missmatch when accessing ID " + id
              + " (expected: numeric; is: " + type.name() + ")");
    }
    return ele.getNumericValue();
  }

  /***
   * <p>Creates a String config item.</p>
   *
   * <p>Creates a config item with a case insensitive identifier.
   * The content of the item may not be null.</p>
   *
   * @param id    Name of config item (case insensitive)
   * @param description Description of value to be written
   * @param dval  Default content if not set
   *
   * @return True if item did not exist and was successfully created
   */
  public boolean createStringConfigValue(String id, String description, String dval) {
    synchronized (configurationData) {
      if (configurationData.get(id.toLowerCase()) == null) {
        ConfigElement ele = new ConfigElement(id, "STRING", description);
        configurationData.put(id.toLowerCase(), ele);
        ele.setDefaultValue(dval);
        LOGGER.log(Level.INFO, "Created String config variable " + id.toLowerCase());
        return true;
      } else {
        return false;
      }
    }
  }

  /***
   * <p>Set a String value to a config parameter.</p>
   *
   * @throws NullPointerException when id is unknown or value is null
   * @throws ClassCastException   when id is not a String setting
   */
  public String setStringValue(String id, String value) {
    ConfigElement ele = configurationData.get(id.toLowerCase());
    if (ele == null || value == null) {
      throw new NullPointerException("unable to get id " + id + " from config subsystem");
    }
    ConfigType type = ele.getType();
    if (type != ConfigType.STRING) {
      throw new ClassCastException("Unable to cast type to correct class (expected: string; is: "
              + type.name() + ")");
    }
    return ele.setStringValue(value);
  }

  /***
   * <p>Sets the value of a string type.</p>
   *
   * @param id    the id of the value to be retrieved
   *
   * @throws NullPointerException when id is unknown
   * @throws ClassCastException   when id is not a String setting
   */
  public String getStringValue(String id) {
    ConfigElement ele = configurationData.get(id.toLowerCase());
    if (ele == null) {
      throw new NullPointerException("unable to get id " + id + " from config subsystem");
    }
    ConfigType type = ele.getType();
    if (type != ConfigType.STRING) {
      throw new ClassCastException("Unable to cast type to correct class (expected: string; is: "
              + type.name() + ")");
    }
    return ele.getStringValue();
  }

}
