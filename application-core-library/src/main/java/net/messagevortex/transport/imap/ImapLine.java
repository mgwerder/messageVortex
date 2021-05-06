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

import net.messagevortex.MessageVortexLogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

// FIXME Limit strings and literals to a certain length (otherwise it is litterally unlimited)
// FIXME Code will fail if Line.length()&gt;Maxint or out of memory

/***
 * <p>A Imap conformant parser/scanner.</p>
 *
 * @author Martin Gwerder
 *
 */
public class ImapLine {

  /* specifies the context range which is given when an exception arises during scanning */
  private static final int MAX_CONTEXT = 30;

  /* These are character subsets specified in RFC3501 */
  private static final String ABNF_SP = " ";
  private static final String ABNF_CTL = charlistBuilder(0, 31);
  private static final String ABNF_LIST_WILDCARDS = "*%";
  private static final String ABNF_QUOTED_SPECIALS = "\"\\";
  private static final String ABNF_RESP_SPECIALS = "[";
  private static final String ABNF_ATOM_SPECIALS = "(){" + ABNF_SP + ABNF_CTL
      + ABNF_LIST_WILDCARDS
      + ABNF_QUOTED_SPECIALS
      + ABNF_RESP_SPECIALS;
  private static final String ABNF_ATOM_CHAR = charlistDifferencer(
      charlistBuilder(1, 127),
      ABNF_ATOM_SPECIALS
  );
  private static final String ABNF_TEXT_CHAR = charlistDifferencer(
      charlistBuilder(1, 127),
      "\r\n"
  );
  private static final String ABNF_QUOTED_CHAR = charlistDifferencer(
      ABNF_TEXT_CHAR,
      ABNF_QUOTED_SPECIALS
  );
  private static final String ABNF_TAG = charlistDifferencer(ABNF_ATOM_CHAR, "+");

  /* a Logger  for logging purposes */
  private static final Logger LOGGER = MessageVortexLogger.getLogger(
      (new Throwable()).getStackTrace()[0].getClassName());
  private static final Object TAG_ENUMERATOR_LOCK = new Object();
  private static int tagEnumerator = 0;
  /* this holds the past context for the case that an exception is risen */
  private String context = "";
  /* storage for the connection which created the line */
  private final ImapConnection con;

  /* The holder for the parsed command/status of a line */
  private String commandToken = null;

  /* The holder for the parsed tag of a line */
  private String tagToken = null;

  /* This input stream is the source of subsequent characters */
  private InputStream input = null;

  /* this is the buffer of read but unprocessed characters */
  private String buffer = "";

  // FIXME should be a ABNF implementation
  // FIXME extract reading from constructor

  /***
   * <p>Creates an imap line object with a parser for a command.</p>
   *
   * <p>A passed input stream is appended to line. Reading takes place according to the
   * ABNF-Rules defined in the respective RFC.</p>
   *
   * @param con   The ImapConnection object which generated the Command line
   * @param line  The String which has already been read (as Read ahead)
   * @param input The Stream offering more data to read if required
   *
   * @throws ImapException if reading fails
   * @throws NullPointerException if connection and line are null
   *
   */
  public ImapLine(ImapConnection con, String line, InputStream input) throws ImapException {
    this.con = con;
    this.input = input;
    this.buffer = line;

    // check if nothing at all (no even an empty line) has been passed
    if (this.buffer == null && this.input == null) {
      throw new ImapNullLineException(this);
    }

    // make sure that we have a valid InputStream
    if (this.input == null) {
      this.input = new ByteArrayInputStream("".getBytes(Charset.defaultCharset()));
    }

    prepareStorage(con, line, input);

    checkEmptyLine();

    prefillCommandFields();

    if ("BAD".equalsIgnoreCase(commandToken) || "OK".equalsIgnoreCase(commandToken)
        || "*".equals(tagToken)) {
      LOGGER.log(Level.INFO, "Parsing command " + tagToken + " " + commandToken);
    }

    skipWhitespace(-1);
  }

  /***
   * <p>Trivial constructor omiting a stream.</p>
   *
   * <p>This constructor is mainly meant for testing purposes</p>
   *
   * @param con   The ImapConnection object which generated the Command line
   * @param line  The String which has already been read (as Read ahead)
   *
   * @throws ImapException if reading fails
   * @throws NullPointerException if connection and line are null
   */
  public ImapLine(ImapConnection con, String line) throws ImapException {
    this(con, line, null);
  }

  /***
   * <p>Builds a set of chracters ranging from the ASCII code of start until the ASCII code
   * of end.</p>
   *
   * <p>This helper is mainly used to build ABNF strings.</p>
   *
   * @param start The first ASCII code to be used
   * @param end   The last ASCII code to be used
   * @return the generated character list
   */
  public static String charlistBuilder(int start, int end) {
    // reject chain building if start is not within 0..255
    if (start < 0 || start > 255) {
      return null;
    }

    // reject chain building if end is not within 0..255
    if (end < 0 || end > 255) {
      return null;
    }

    // reject chain building if start>end
    if (end < start) {
      return null;
    }

    // build the string
    StringBuilder ret = new StringBuilder();
    for (int i = start; i <= end; i++) {
      ret.append((char) i);
    }
    return ret.toString();
  }

  /***
   * <p>Removes a given set of characters from a superset.</p>
   *
   * @param superset    the set where character should be removed from
   * @param subset      the set of characters to be removed
   * @return the difference of the two given charsets
   */
  public static String charlistDifferencer(String superset, String subset) {
    String ret = superset;
    for (int i = 0; i < subset.length(); i++) {
      char c = subset.charAt(i);
      ret = ret.replace("" + c, "");
    }
    return ret;
  }

  /***
   * <p>Encodes a command so that newlines are visible.</p>
   *
   * @param command the command to be encoded for output on screen
   * @return a printable string representation
   */
  public static String commandEncoder(String command) {
    if (command == null) {
      return "<null>";
    }
    return command.replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
  }

  /***
   * <p>Get a unique identifier as a tag.</p>
   *
   * @return A unique tag ("A" prefixed)
   */
  public static String getNextTag() {
    return getNextTag("A");
  }

  /***
   * <p>Get a unique identifier as a tag.</p>
   *
   * @param prefix the prefix prepended to the tag
   * @return A unique tag
   */
  public static String getNextTag(String prefix) {
    String ret;
    synchronized (TAG_ENUMERATOR_LOCK) {
      tagEnumerator++;
      ret = prefix + String.format("%d", tagEnumerator);
    }
    return ret;
  }

  private void prepareStorage(ImapConnection con, String line, InputStream input)
      throws ImapException {
    // make sure that a line is never null when reaching the parsing section
    if (buffer == null) {
      buffer = "";
      if (snoopBytes(1) == null) {
        throw new ImapBlankLineException(this);
      }
    }

  }

  private void checkEmptyLine() throws ImapException {
    if ("\r\n".equals(snoopBytes(2)) || "".equals(snoopBytes(1)) || snoopBytes(1) == null) {
      if ("\r\n".equals(snoopBytes(2))) {
        skipUntilLineEnd();
        throw new ImapBlankLineException(this);
      }
      throw new ImapNullLineException(this);
    }
  }

  private void prefillCommandFields() throws ImapException {
    // getting a tag
    tagToken = getATag();

    if (tagToken == null || "".equals(tagToken)) {
      skipUntilLineEnd();
      throw new ImapException(this, "error getting tag");
    }

    int i = skipWhitespace(1);
    if (i != 1) {
      throw new ImapException(this, "error skipping to command (line=\"" + context + "\"; tag="
          + tagToken + "; buffer=" + buffer + "; skipped=" + i + ")");
    }

    // get command
    commandToken = getATag();
    if (commandToken == null || "".equals(commandToken)) {
      skipUntilLineEnd();
      throw new ImapException(this, "error getting command");
    }

  }

  /***
   * <p>Getter for the Imap connection in Control of this command.</p>
   *
   * @return ImapConnection storing the context of this command
   */
  public ImapConnection getConnection() {
    return con;
  }

  /***
   * <p>Getter for the command.</p>
   *
   * @return command token
   */
  public String getCommand() {
    return commandToken;
  }

  /***
   * <p>Getter for the command tag.</p>
   *
   * @return command tag
   */
  public String getTag() {
    return tagToken;
  }

  private boolean readBuffer(long num) {
    // make sure that we have sufficient bytes in the buffer
    boolean ended = false;
    try {
      while (buffer.length() < num && !ended) {
        int i = input.read();
        if (i >= 0) {
          buffer += (char) i;
        } else {
          ended = true;
        }
      }
    } catch (IOException ioe) {
      ended = true;
      LOGGER.log(Level.FINER, "IO exception raised while reading into buffer (" + ioe + ")");
    }
    return ended;
  }

  /***
   * <p>Returns true if escaped quotes are present at the current position.</p>
   *
   * @return true if escaped quotes are present
   */
  public boolean snoopEscQuotes() {
    return "\\".contains(snoopBytes(1))
        && snoopBytes(2).length() == 2
        && ABNF_QUOTED_SPECIALS.contains(snoopBytes(2).substring(1, 2));
  }

  /***
   * <p>Get the specified number of characters without moving from the current position.</p>
   *
   * <p>if num is 0 or negative then null is returned. If the number
   * of available bytes is lower than the number of requested characters
   * then the buffer content is returned.</p>
   *
   * @param num the number of bytes to be snooped
   * @return the requested string
   */
  public String snoopBytes(long num) {
    if (num <= 0) {
      return null;
    }

    // build buffer
    readBuffer(num);

    if (buffer.length() == 0) {
      return null;
    }

    // if the string is too short -> return it
    if (buffer.length() < num) {
      return buffer;
    }

    return buffer.substring(0, (int) num);
  }

  private void addContext(String chunk) {
    context += chunk;
    if (context.length() > MAX_CONTEXT) {
      context = context.substring(context.length() - MAX_CONTEXT);
    }
  }

  /***
   * <p>Returns the current buffer (including position) and some of the already read characters.</p>
   *
   * @return String representation of the current context
   */
  public String getContext() {
    return context + "^^^" + buffer;
  }

  /***
   * <p>Skips the specified number of characters and adds them to the past context.</p>
   *
   * @param   num number of characters to be skipped
   * @return String representation of the skipped characters
   */
  public String skipBytes(long num) {
    return skipBytes(num, true);
  }

  /***
   * <p>Skips the specified number of bytes.</p>
   *
   * @param num        the number of bytes to be skipped
   * @param modContext if true the context is updated by the operation
   * @return the skipped bytes
   */
  public String skipBytes(long num, boolean modContext) {
    // make sure that we have sufficient bytes in the buffer
    readBuffer(num);

    // if the string is too short -> return it
    if (buffer.length() < num) {
      buffer = "";
      if (modContext) {
        addContext(buffer);
      }
      return buffer;
    }

    // prepare return result
    String ret = snoopBytes(num);

    // update context
    if (modContext) {
      addContext(ret);
    }

    // cut the buffer
    buffer = buffer.substring((int) num);

    return ret;
  }

  /***
   * <p>Skips the specified number of SPACES.</p>
   *
   * @param   num number of spaces to be skipped
   * @return number of skipped spaces
   */
  public final int skipWhitespace(int num) {
    // count number of spaces found
    int count = 0;

    // init countdown counter
    int countdown = num;

    LOGGER.log(Level.FINER, "Skipping " + num + " spaces");
    // loop thru skipper
    while ((countdown != 0) && snoopBytes(1) != null && ABNF_SP.contains(snoopBytes(1))) {
      skipBytes(1);
      count++;
      countdown--;
    }
    LOGGER.log(Level.FINER, "Skipped " + count + " spaces");

    // return count of spaces skipped
    return count;
  }

  /***
   * <p>Skips a CRLF combo in the buffer.</p>
   *
   * @return True if a combo has been skipped
   */
  public boolean skipLineEnd() {
    LOGGER.log(Level.FINER, "Skipping CRLF");
    if (snoopBytes(2) != null && "\r\n".equals(snoopBytes(2))) {
      skipBytes(2);
      LOGGER.log(Level.FINER, "CRLF skipped");
      return true;
    }
    LOGGER.log(Level.FINER, "no CRLF found");
    return false;
  }

  /***
   * <p>Skips up to a CRLF combo in the buffer.</p>
   *
   * @return True if a combo has been skipped (false if buffer ended before a CRLF combo was read
   */
  public boolean skipUntilLineEnd() {
    while (snoopBytes(2) != null && !"\r\n".equals(snoopBytes(2))) {
      skipBytes(1, false);
    }

    if (snoopBytes(2) != null && "\r\n".equals(snoopBytes(2))) {
      skipBytes(2, false);
      return true;
    }
    return false;
  }

  private long getLengthPrefix() {
    //skip curly brace
    skipBytes(1);

    // get number

    long num = 0;
    while (num < 4294967295L && snoopBytes(1) != null && "0123456789".contains(snoopBytes(1)) ) {
      num = num * 10 + (int) (skipBytes(1).charAt(0)) - (int) ("0".charAt(0));
    }
    return num;
  }

  private String getLengthPrefixedString() {

    long num = getLengthPrefix();

    if (num < 0 || num > 4294967295L) {
      return null;
    }

    // skip rcurly brace
    String ret = skipBytes(1);
    if (!"}".equals(ret)) {
      return null;
    }

    // skip crlf
    skipLineEnd();

    // get String
    ret = skipBytes(num);

    if (ret == null || ret.length() < num) {
      return null;
    }

    return ret;
  }

  private String getQuotedString() {
    // get a quoted string
    skipBytes(1);
    StringBuilder ret = new StringBuilder();
    while (snoopBytes(1) != null && (ABNF_QUOTED_CHAR.contains(snoopBytes(1))
        || snoopEscQuotes())) {
      if ("\\".contains(snoopBytes(1))) {
        ret.append(skipBytes(2).charAt(1));
      } else {
        ret.append(skipBytes(1));
      }
    }
    if (snoopBytes(1) == null || !"\"".equals(skipBytes(1))) {
      return null;
    }

    return ret.toString();
  }

  /***
   * <p>Get an IMAP String from the buffer (quoted or prefixed).</p>
   *
   * @return The String or null if no string is at the current position
   */
  public String getString() {
    String start = snoopBytes(1);
    String ret = null;

    if ("{".equals(start)) {

      return getLengthPrefixedString();

    } else if ("\"".equals(start)) {

      return getQuotedString();

    }

    return ret;
  }

  /***
   * <p>Get an IMAP AString (direct, quoted or prefixed) from the current buffer position.</p>
   *
   * @return The String or null if no string at the current position
   */
  public String getAString() {

    String start = snoopBytes(1);
    String ret = null;

    if ("{".equals(start) || "\"".equals(start)) {

      // get a dquoted or literal (length prefixed) String
      ret = getString();

    } else {

      // get a sequence of atom chars
      while (snoopBytes(1) != null && ABNF_ATOM_CHAR.contains(snoopBytes(1))) {
        if (ret == null) {
          ret = "";
        }
        ret += skipBytes(1);
      }
    }

    return ret;
  }

  /***
   * <p>Get the tag at the current position.</p>
   *
   * @return the tag or null if no valid is found
   */
  public String getATag() {
    String ret = null;

    // get a sequence of atom chars
    while (snoopBytes(1) != null && ABNF_TAG.contains(snoopBytes(1))) {
      if (ret == null) {
        ret = "";
      }
      ret += skipBytes(1);
    }

    if ("".equals(ret)) {
      // empty tags are not alowed. At least one char is required
      return null;
    }

    return ret;
  }
}
