package net.gwerder.java.messagevortex;

// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// ************************************************************************************

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Pattern;

public class MessageVortexConfig extends Config {

  private MessageVortexConfig() throws IOException {
    // This constructor hides a default constructor
    this("messageVortex.cfgRessources");
  }

  private MessageVortexConfig(String ressourceFile) throws IOException {
    try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(Files.newInputStream(Paths.get(ressourceFile)),
                    StandardCharsets.UTF_8))) {
      String line = reader.readLine();
      while (line != null) {
        if (Pattern.matches("\\s*//.*", line)) {
          // ignore comment lines
        } else if (Pattern.matches("\\s*", line)) {
          // ignore  empty lines
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
              } else if ("string".equals(token.toLowerCase())) {
                String name = scanner.next().trim();
                String val = scanner.next().trim();
                if ("".equals(val)) {
                  val = null;
                }
                String desc = scanner.next().trim();
                createStringConfigValue(name, desc, val);
              } else if ("numeric".equals(token.toLowerCase())) {
                String name = scanner.next().trim();
                int val = Integer.parseInt(scanner.next().trim());
                String desc = scanner.next().trim();
                createNumericConfigValue(name, desc, val);
              } else {
                throw new IOException("encountered unknown field type: " + token
                        + " (line was \"" + line + "\")");
              }
            }
          }
        }
        line = reader.readLine();
      }
    }
  }

  public static Config createConfig() throws IOException {
    Config cfg = new MessageVortexConfig();
    if (defaultConfig == null) {
      defaultConfig = cfg;
    }
    return cfg;
  }

}
