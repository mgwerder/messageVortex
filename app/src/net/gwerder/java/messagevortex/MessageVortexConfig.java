package net.gwerder.java.messagevortex;

/**
 * Created by Martin on 27.01.2018.
 */
public class MessageVortexConfig extends Config {

    public static Config getDefault() {
        if(defaultConfig==null) {
            defaultConfig=new MessageVortexConfig();
        }
        return defaultConfig;
    }

    private MessageVortexConfig() {
        // This constructor hides a default constructor
    }

    @Override
    public Config createConfig() {
        return new MessageVortexConfig();
    }
}
