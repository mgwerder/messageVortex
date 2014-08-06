package net.gwerder.java.mailvortex;

import java.util.concurrent.ConcurrentHashMap;

public class Config {

    private static ConcurrentHashMap<String,Object> configurationData= new ConcurrentHashMap<String,Object>();

    public static boolean createBooleanConfigValue(String id,boolean dval) {
        synchronized(configurationData) {
            if(configurationData.get(id.toLowerCase())==null) {
                configurationData.put(id.toLowerCase(),new Boolean(dval));
                return true;
            } else {
                return false;
            }
        }    
    }

    public static boolean setBooleanValue(String id,boolean value) throws NullPointerException,ClassCastException {
        boolean ret;
        if(configurationData.get(id.toLowerCase())==null) {
            throw new NullPointerException();
        } else if(!(configurationData.get(id.toLowerCase()) instanceof Boolean)) {
            throw new ClassCastException();
        } else {
            ret=getBooleanValue(id);
            configurationData.put(id.toLowerCase(),new Boolean(value));
        }    
        return ret;
    }

    public static boolean getBooleanValue(String id) throws NullPointerException,ClassCastException {
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