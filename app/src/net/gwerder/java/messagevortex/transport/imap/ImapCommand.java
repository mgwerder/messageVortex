package net.gwerder.java.messagevortex.transport.imap;

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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ImapCommand implements Cloneable {

  private static final Map<String, ImapCommand> COMMANDS;

  static {
    COMMANDS = new ConcurrentHashMap<>();
    (new ImapCommandCapability()).init();
    (new ImapCommandLogin()).init();
    (new ImapCommandAuthenticate()).init();
    (new ImapCommandLogout()).init();
    (new ImapCommandNoop()).init();
  }

  public static void registerCommand(ImapCommand command) {
    String[] arr = command.getCommandIdentifier();
    for (String a : arr) {
      COMMANDS.put(a.toLowerCase(), command);
    }
  }

  public static void deregisterCommand(String command) {
    COMMANDS.remove(command.toLowerCase());
  }

  /***
   * <p>Returns a list of all supported ImapCommands in no particular order.</p>
   *
   * <p>The returned list is independent of any state.</p>
   *
   * @return an array containing all ImapCommands available at any state
   */
  public static ImapCommand[] getCommands() {
    return COMMANDS.values().toArray(new ImapCommand[COMMANDS.size()]);
  }

  public static ImapCommand getCommand(String name) {
    return COMMANDS.get(name.toLowerCase());
  }

  public abstract String[] getCapabilities(ImapConnection conn);

  public abstract void init();

  public abstract String[] getCommandIdentifier();

  /***
   * <p>Processes the imap lie prefixed by a command returned by getCommandIdentifier().</p>
   *
   * @param line the line containing the command to be processed
   * @return multilined server reply (if any)
   * @throws ImapException
   */
  public abstract String[] processCommand(ImapLine line) throws ImapException;

}
