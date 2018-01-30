package net.gwerder.java.messagevortex;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public abstract class Config {

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    private enum ConfigType {
        BOOLEAN,
        STRING;

        public static ConfigType getById( String id ) {
            for( ConfigType c:values() ) {
                if(c.name().toLowerCase().equals( id.toLowerCase() ) ) {
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

        public ConfigElement( String id, String type ) {
            setId( id );
            setType( type );
            setDefaultValue( null );
            setDescription( null );
        }

        public ConfigElement( String id, String type, String description ) {
            this( id, type );
            setDescription( description );
        }

        public ConfigElement copy() {
            ConfigElement ret = new ConfigElement( id, type, description );
            setDefaultValue( defaultValue );
            setValue( currentValue );
            return ret;
        }

        public void setId(String id) {
            if( id==null ) {
                throw new NullPointerException("id must not be null");
            }
            this.id=id.toLowerCase();
        }

        public void setType(String type) {
            if( type == null ) {
                throw new NullPointerException("type must not be null");
            } else if( ConfigType.getById( type ) == null ) {
                throw new IllegalArgumentException( "type "+type+" is not a known config type" );
            } else {
                this.type = type.toLowerCase();
            }
        }

        public ConfigType getType() {
            return ConfigType.getById( this.type );
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getStringValue() {
            if( currentValue != null ) {
                return currentValue;
            } else {
                return defaultValue;
            }
        }

        public String setStringValue( String value ) {
            String ret = getStringValue();
            currentValue=value;
            return ret;
        }

        public boolean getBooleanValue() {
            String ret=getStringValue();
            return ret != null && ("true".equals(ret.toLowerCase()) || "yes".equals(ret.toLowerCase()));
        }

        public boolean setBooleanValue( boolean value ) {
            boolean ret=getBooleanValue();
            if(value) {
                currentValue="true";
            } else {
                currentValue="false";
            }
            return ret;
        }

        public String unset() {
            return setValue( null );
        }

        private String setValue( String value ) {
            String ret=getStringValue();
            if( defaultValue != null && defaultValue.equals(value) ) {
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

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

    }

    static Config defaultConfig=null;
    private final Map<String,ConfigElement> configurationData= new ConcurrentHashMap<>();
    private final List<ConfigElement> orderedListOfConfigElements = new Vector<>();

    /* Gets a boolean value from the application config.
     *
     * @param id key which should be set
     * @param value Vlue to be set in key
     * @returns old value before setting to new value
     * @throws NullPointerException if key does not exist in configurationData
     * @throws ClassCastException if key is not of type boolean
     ***/
    public static Config getDefault() throws IOException {
        return defaultConfig;
    }

    public static Config copy(Config src) throws IOException {
        Config dst=src.createConfig();
        synchronized(src.configurationData) {
            Set<Map.Entry<String,ConfigElement>> it = src.configurationData.entrySet();
            for(Map.Entry<String,ConfigElement> p:it) {
                dst.configurationData.put(p.getKey(),p.getValue().copy());
            }
        }
        return dst;
    }

    public abstract Config createConfig() throws IOException;

    public void createBooleanConfigValue(String id, String description,boolean dval) {
        synchronized (configurationData) {
            if (configurationData.get( id.toLowerCase()) == null ) {
                ConfigElement ele = new ConfigElement( id,"boolean", description );
                configurationData.put( id.toLowerCase(), ele );
                ele.setDefaultValue(dval?"true":"false");
                LOGGER.log(Level.FINE,"Created boolean config variable "+id.toLowerCase());
            } else {
                throw new IllegalArgumentException( "id \"" + id + "\" is already defined" );
            }
        }
    }

    /***
     * Returns a deep copy of this config store.
     ***/
    public Config copy() throws IOException {
        return copy(this);
    }

    /***
     * Gets a boolean value from the application config.
     *
     * @param id key which should be set
     * @param value Vlue to be set in key
     * @return old value before setting to new value
     * @throws NullPointerException if key does not exist in configurationData
     * @throws ClassCastException if key is not of type boolean
     ***/
    public boolean setBooleanValue(String id,boolean value) throws IOException {
        boolean ret;
        ConfigElement ele = configurationData.get(id.toLowerCase());
        if( ele == null ) {
            throw new NullPointerException( "id "+id+" is not known to the config subsystem" );
        } else if( ele.getType() != ConfigType.BOOLEAN ) {
            throw new ClassCastException("config type missmatch when accessing ID "+id+" (expected: boolean; is: "+ele.getType().name()+")" );
        } else {
            ret=ele.getBooleanValue();
            ele.setBooleanValue( value );
        }
        return ret;
    }

    /***
     * Gets a boolean value from the application config.
     *
     * @param id          key which should be set
     * @return                current value of the specified key
     * @throws NullPointerException if key does not exist in configurationData
     * @throws ClassCastException if key is not of type boolean
     ***/
    public boolean getBooleanValue(String id) throws IOException {
        boolean ret;
        ConfigElement ele = configurationData.get(id.toLowerCase());
        if( ele == null ) {
            throw new NullPointerException( "id "+id+" is not known to the config subsystem" );
        } else if( ele.getType() != ConfigType.BOOLEAN ) {
            throw new ClassCastException("config type missmatch when accessing ID "+id+" (expected: boolean; is: "+ele.getType().name()+")" );
        } else {
            ret=ele.getBooleanValue();
        }
        return ret;
    }

    /***
     * Creates a String config item.
     *
     * Creates a config item with a case insensitive identifier.
     * The content of the item may not be null.
     *
     * @param id    Name of config item (case insensitive)
     * @param description Description of value to be written
     * @param dval  Default content if not set
     *
     * @return     True if item did not exist and was successfully created
     ***/
    public boolean createStringConfigValue( String id, String description, String dval ) {
        synchronized(configurationData) {
            if( configurationData.get(id.toLowerCase()) == null ) {
                ConfigElement ele = new ConfigElement( id, "STRING", description );
                configurationData.put( id.toLowerCase(), ele );
                ele.setDefaultValue( dval );
                LOGGER.log(Level.INFO,"Created String config variable "+id.toLowerCase() );
                return true;
            } else {
                return false;
            }
        }
    }

    /***
     * @throws NullPointerException when id is unknown or value is null
     * @throws ClassCastException   when id is not a String setting
     ***/
    public String setStringValue(String id,String value) {
        String ret;
        ConfigElement ele = configurationData.get(id.toLowerCase());
        if( ele == null || value==null) {
            throw new NullPointerException( "unable to get id "+id+" from config subsystem");
        } else if( ele.getType() != ConfigType.STRING) {
            throw new ClassCastException("Unable to cast type to correct class (expected: string; is: "+ele.getType().name()+")" );
        } else {
            ret=ele.getStringValue();
            ele.setStringValue( value );
        }
        return ret;
    }

    /***
     * @throws NullPointerException when id is unknown
     * @throws ClassCastException   when id is not a String setting
     ***/
    public String getStringValue(String id) {
        String ret;
        ConfigElement ele = configurationData.get( id.toLowerCase() );
        if( ele == null ) {
            throw new NullPointerException( "unable to get id "+id+" from config subsystem");
        } else if( ele.getType() != ConfigType.STRING) {
            throw new ClassCastException("Unable to cast type to correct class (expected: string; is: "+ele.getType().name()+")" );
        } else {
            ret=ele.getStringValue();
        }
        return ret;
    }

}
