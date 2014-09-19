package net.gwerder.java.mailvortex;

import java.net.URL;

public class Version {

    private static int    MAJOR       = @major@;
    private static int    MINOR       = @minor@;
    private static int    REVISION    = @revision@;
    private static String BUILD       = "@build@";
  
    private static String VERSION     = MAJOR+"."+MINOR+"."+REVISION;
    private static String BUILDVER    = VERSION+" ("+BUILD+")";
    
    private Version() {
        super();
    }
  
    public static String getBuild() {
        return BUILDVER;
    }

    public static String getVersion() {
        return VERSION;
    }
    
    static {
        URL main = Version.class.getResource("Version.class");
        if (!"file".equalsIgnoreCase(main.getProtocol())) {
            throw new IllegalStateException("Main class is not stored in a file.");
        }
        // can be asked System.out.println( "Path to application is "+main.getPath());    
        // can be asked System.out.println( "Path to user.dir is "+System.getProperty("user.dir"));    
    }
 }   