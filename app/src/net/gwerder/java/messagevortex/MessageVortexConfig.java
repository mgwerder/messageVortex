package net.gwerder.java.messagevortex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Created by Martin on 27.01.2018.
 */
public class MessageVortexConfig extends Config {

    public static Config getDefault() throws IOException {
        if(defaultConfig==null) {
            defaultConfig = new MessageVortexConfig();
        }
        return defaultConfig;
    }

    private MessageVortexConfig() throws IOException{
        // This constructor hides a default constructor
        this("messageVortex.cfgRessources");
    }

    private MessageVortexConfig(String ressourceFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(ressourceFile)) ) {
            String line=reader.readLine();
            while(line!=null) {
                if(Pattern.matches("\\s*//.*",line)) {
                    // ignore comment lines
                } else if( Pattern.matches( "\\s*",line) ) {
                    // ignore empty lines
                } else {
                    try (Scanner scanner = new Scanner(line)) {
                        scanner.useDelimiter("\\s*,\\s*");
                        while (scanner.hasNext()) {
                            String token = scanner.next();
                            if ("boolean".equals(token.toLowerCase())) {
                                String name = scanner.next().trim();
                                boolean val = "true".equals(scanner.next().toLowerCase().trim());
                                String desc = scanner.next().trim();
                                createBooleanConfigValue(name, desc, val);
                            } else if ("string".equals(token.toLowerCase()) || "numeric".equals(token.toLowerCase())) {
                                String name = scanner.next().trim();
                                String val = scanner.next().trim();
                                if( "".equals(val) ) {
                                    val=null;
                                }
                                String desc = scanner.next().trim();
                                createStringConfigValue(name, desc, val);
                            } else {
                                throw new IOException("encountered unknown field type: " + token +" (line was \""+line+"\")");
                            }
                        }
                    }
                }
                line=reader.readLine();
            }
        }
    }

    @Override
    public Config createConfig()throws IOException {
        return new MessageVortexConfig();
    }

}
