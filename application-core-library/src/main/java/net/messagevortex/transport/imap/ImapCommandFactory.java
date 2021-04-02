package net.messagevortex.transport.imap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public  class ImapCommandFactory {

  private static final Map<String, ImapCommand> COMMANDS;

  static {
    COMMANDS = new ConcurrentHashMap<>();
    registerCommand(new ImapCommandCapability());
    registerCommand(new ImapCommandLogin());
    registerCommand(new ImapCommandAuthenticate());
    registerCommand(new ImapCommandLogout());
    registerCommand(new ImapCommandNoop());
  }

  /***
   * <p>register a command so that process command is able to identify it and call the apropriate
   * handler.</p>
   *
   * @param command the command to be registered as known command
   */
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
    return COMMANDS.values().toArray(new ImapCommand[0]);
  }

  public static ImapCommand getCommand(String name) {
    return COMMANDS.get(name.toLowerCase());
  }
}
