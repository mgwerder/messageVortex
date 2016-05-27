package net.gwerder.java.mailvortex;

public class Version {

    private static int    MAJOR       = 1; //@major@
    private static int    MINOR       = 0; //@minor@
    private static int    REVISION    = 0; //@revision@
    private static String SVN_BUILD   = "$Revision$"; //@build@
    private static String BUILD       = SVN_BUILD.replaceAll( "\\$Revision: *","" ).replaceAll( " *\\$$","" );
  
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
    
}   