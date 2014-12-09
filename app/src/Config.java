package net.gwerder.java.mailvortex;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Iterator;

public class Config {

    private static final Config DEFAULT_CFG=new Config();

    private Map<String,Object> configurationData= new ConcurrentHashMap<String,Object>();
    
    public boolean createBooleanConfigValue(String id,boolean dval) {
        synchronized(configurationData) {
            if(configurationData.get(id.toLowerCase())==null) {
                configurationData.put(id.toLowerCase(),Boolean.valueOf(dval));
                return true;
            } else {
                return false;
            }
        }    
    }
    
    public static Config getDefault() {
        return DEFAULT_CFG;
    }    
    
    public static Config copy(Config src) {
        Config dst=new Config();
        synchronized(src.configurationData) {
            Iterator it = src.configurationData.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry p=(Map.Entry)it.next();
                if(p.getValue() instanceof Boolean) {
                    dst.configurationData.put((String)p.getKey(),Boolean.valueOf(((Boolean)(p.getValue())).booleanValue()));
                } else if(p.getValue() instanceof String) {
                    dst.configurationData.put((String)p.getKey(),p.getValue());
                } else {
                    throw new ClassCastException("unknown value in config data");
                }
            }                
        }  
        return dst;
    }
    
    /***
     * Returns a deep copy of this config store.
     ***/
    public Config copy() {
        return copy(this);
    }
    
    /***
     * @throws NullPointerException when id is unknown
     * @throws ClassCastException   when id is not a boolean setting
     ***/
    public boolean setBooleanValue(String id,boolean value) {
        boolean ret;
        if(configurationData.get(id.toLowerCase())==null) {
            throw new NullPointerException();
        } else if(!(configurationData.get(id.toLowerCase()) instanceof Boolean)) {
            throw new ClassCastException();
        } else {
            ret=getBooleanValue(id);
            configurationData.put(id.toLowerCase(),Boolean.valueOf(value));
        }    
        return ret;
    }

    /***
     * @throws NullPointerException when id is unknown
     * @throws ClassCastException   when id is not a boolean setting
     ***/
    public boolean getBooleanValue(String id) {
        boolean ret;
        if(configurationData.get(id.toLowerCase())==null) {
            throw new NullPointerException();
        } else if(!(configurationData.get(id.toLowerCase()) instanceof Boolean)) {
            throw new ClassCastException();
        } else {
            ret=((Boolean)(configurationData.get(id.toLowerCase()))).booleanValue();
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