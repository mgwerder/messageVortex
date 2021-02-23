package net.messagevortex.transport.imap;
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

public class ImapCommandCapability extends ImapCommand {

  public void init() {
    ImapCommandFactory.registerCommand(this);
  }

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
