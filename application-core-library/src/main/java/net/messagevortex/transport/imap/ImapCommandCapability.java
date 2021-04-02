package net.messagevortex.transport.imap;


public class ImapCommandCapability extends ImapCommand {

  private String addCapability(String fullCap, String cap) throws ImapException {
    String t = fullCap;
    if (cap.indexOf('=') == -1) {
      t += " " + cap;
    } else {
      String[] v = cap.split("=");
      if (v.length != 2) {
        throw new ImapException(null, "got illegal capability \"" + cap + "\" from command");
      }
      if (t.indexOf(v[0] + "=") > -1) {
        t = t.replace(v[0] + "=", v[0] + "=" + v[1] + ",");
      } else {
        t += " " + cap;
      }
    }
    return t;
  }

  @Override
  public String[] processCommand(ImapLine line) throws ImapException {

    // skip space
    // WARNING this is "non-strict"
    line.skipWhitespace(-1);

    // skip lineend
    if (!line.skipLineEnd()) {
      throw new ImapException(line, "error parsing command");
    }

    ImapCommand[] arr = ImapCommandFactory.getCommands();
    String cap = "";

    // looping thru commands
    for (ImapCommand ic : arr) {

      String[] arr2 = ic.getCapabilities(line.getConnection());
      if (arr2 != null) {
        for (String a2 : arr2) {
          cap = addCapability(cap, a2);
        }
      }
    }
    return new String[]{"* CAPABILITY IMAP4rev1" + cap + "\r\n", line.getTag() + " OK\r\n"};
  }

  public String[] getCommandIdentifier() {
    return new String[]{"CAPABILITY"};
  }

  public String[] getCapabilities(ImapConnection conn) {
    return new String[]{};
  }

}
