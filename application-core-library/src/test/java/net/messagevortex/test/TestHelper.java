package net.messagevortex.test;

/**
 * Created by Martin on 10.05.2017.
 */
public class TestHelper {

    private static String compareStrings(String s1,String s2) {
        int line = 1;
        int lfpos=0;
        int col=1;
        for(int i=0;i<Math.max(s1.length(),s2.length());i++) {
            if(s1.charAt(i)!=s2.charAt(i)) {
                // difference detected
                String reply= "Difference detected at line "+line+" column "+col+"\n";
                reply+=""+s1.substring(lfpos,i)+"\n";
                reply+=s1+"\n"+s2+"\n";
                return reply;
            }
            if(s1.charAt(i)=='\n') {
                col=0;
                lfpos=i+1;
                line++;
            }
            col++;
        }
        return "no differences detected\n";
    }

    public static String compareDumps(String s1,String s2) {
        return compareStrings(prepareDump(s1),prepareDump(s2));
    }

    public static String prepareDump(String s) {
        return s.replaceAll( "encrypted *'[^']*'H","encrypted '<encryptedString>'H");
    }

}
