package net.gwerder.java.mailvortex;

public class Version {

  public static int  MAJOR         = @major@;
  public static int  MINOR         = @minor@;
  public static int  REVISION = @revision@;;
  public static String BUILD     = "@build@";
  
  public static String VERSION = ""+MAJOR+"."+MINOR+"."+REVISION+" ("+BUILD+")";

}
