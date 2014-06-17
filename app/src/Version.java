package net.gwerder.java.mailvortex;

public class Version {

    private static int    MAJOR       = @major@;
    private static int    MINOR       = @minor@;
    private static int    REVISION    = @revision@;
    private static String BUILD       = "@build@";
  
    private static String VERSION     = MAJOR+"."+MINOR+"."+REVISION;
    private static String BUILDVER    = VERSION+" ("+BUILD+")";
  
    public static String getBuild() {
        return BUILDVER;
    }

    public static String getVersion() {
        return VERSION;
    }
 }   