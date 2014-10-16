package net.gwerder.java.mailvortex;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class Config {

    private static Map<String,Object> configurationData= new ConcurrentHashMap<String,Object>();
    
    private Config() {
        super();
    }

    public static boolean createBooleanConfigValue(String id,boolean dval) {
        synchronized(configurationData) {
            if(configurationData.get(id.toLowerCase())==null) {
                configurationData.put(id.toLowerCase(),Boolean.valueOf(dval));
                return true;
            } else {
                return false;
            }
        }    
    }
    
    /***
     * @throws NullPointerException when id is unknown
     * @throws ClassCastException   when id is not a boolean setting
     ***/
    public static boolean setBooleanValue(String id,boolean value) {
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
    public static boolean getBooleanValue(String id) {
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

}