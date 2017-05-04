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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Config {

    private static final Config DEFAULT_CFG=new Config();

    private final Map<String,Object> configurationData= new ConcurrentHashMap<>();

    /* Gets a boolean value from the application config.
     *
     * @param id key which should be set
     * @param value Vlue to be set in key
     * @returns old value before setting to new value
     * @throws NullPointerException if key does not exist in configurationData
     * @throws ClassCastException if key is not of type boolean
     ***/
    public static Config getDefault() {
        return DEFAULT_CFG;
    }

    public static Config copy(Config src) {
        Config dst=new Config();
        synchronized(src.configurationData) {
            Set<Map.Entry<String,Object>> it = src.configurationData.entrySet();
            for(Map.Entry<String,Object> p:it) {
                //noinspection ChainOfInstanceofChecks
                if(p.getValue() instanceof Boolean) {
                    dst.configurationData.put(p.getKey(),p.getValue());
                } else if(p.getValue() instanceof String) {
                    dst.configurationData.put(p.getKey(),p.getValue());
                } else {
                    throw new ClassCastException("unknown value in config data");
                }
            }
        }
        return dst;
    }

    public boolean createBooleanConfigValue(String id, boolean dval) {
        synchronized (configurationData) {
            if (configurationData.get(id.toLowerCase()) == null) {
                configurationData.put(id.toLowerCase(), dval);
                return true;
            } else {
                return false;
            }
        }
    }

    /***
     * Returns a deep copy of this config store.
     ***/
    public Config copy() {
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
    public boolean setBooleanValue(String id,boolean value) {
        boolean ret;
        if(getDefault().configurationData.get(id.toLowerCase())==null) {
            throw new NullPointerException();
        } else if(!(getDefault().configurationData.get(id.toLowerCase()) instanceof Boolean)) {
            throw new ClassCastException();
        } else {
            ret=getBooleanValue(id);
            getDefault().configurationData.put(id.toLowerCase(),value);
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
    public boolean getBooleanValue(String id) {
        boolean ret;
        if(getDefault().configurationData.get(id.toLowerCase())==null) {
            throw new NullPointerException();
        } else if(!(getDefault().configurationData.get(id.toLowerCase()) instanceof Boolean)) {
            throw new ClassCastException();
        } else {
            ret=(Boolean)(getDefault().configurationData.get(id.toLowerCase()));
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
     * @param dval  Default content if not set
     *
     * @return     True if item did not exist and was successfully created
     ***/
    public boolean createStringConfigValue(String id,String dval) {
        synchronized(configurationData) {
            if(configurationData.get(id.toLowerCase())==null && dval!=null) {
                configurationData.put(id.toLowerCase(),dval);
                return true;
            } else {
                return false;
            }
        }
    }

    /***
     * @throws NullPointerException when id is unknown
     * @throws ClassCastException   when id is not a String setting
     ***/
    public String setStringValue(String id,String value) {
        String ret;
        if(configurationData.get(id.toLowerCase())==null || value==null) {
            throw new NullPointerException();
        } else if(!(configurationData.get(id.toLowerCase()) instanceof String)) {
            throw new ClassCastException();
        } else {
            ret=getStringValue(id);
            configurationData.put(id.toLowerCase(),value);
        }
        return ret;
    }

    /***
     * @throws NullPointerException when id is unknown
     * @throws ClassCastException   when id is not a String setting
     ***/
    public String getStringValue(String id) {
        String ret;
        if(configurationData.get(id.toLowerCase())==null) {
            throw new NullPointerException();
        } else if(!(configurationData.get(id.toLowerCase()) instanceof String)) {
            throw new ClassCastException();
        } else {
            ret=(String)(configurationData.get(id.toLowerCase()));
        }
        return ret;
    }

}
