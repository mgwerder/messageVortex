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

public class ImapCommandNoop extends ImapCommand {

  /***
   * <p>Process NOOP command.</p>
   *
   * @param line the full line to be processed.
   * @return the server reply
   */
  public String[] processCommand(ImapLine line) throws ImapException {

    // skip space
    // WARNING this is "non-strict"
    line.skipWhitespace(-1);

    // skip line end
    if (!line.skipLineEnd()) {
      throw new ImapException(line, "error parsing command");
    }

    // Example:
    //// * 22 EXPUNGE
    //// * 23 EXISTS
    //// * 3 RECENT
    //// * 14 FETCH (FLAGS (\Seen \Deleted))
    // FIXME status probably wrong (returns always OK)
    return new String[]{line.getTag() + " OK\r\n"};
  }

  public String[] getCommandIdentifier() {
    return new String[]{"NOOP"};
  }

  public String[] getCapabilities(ImapConnection conn) {
    return new String[]{};
  }

}
