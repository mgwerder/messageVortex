package net.messagevortex.transport.imap;

public abstract class ImapCommand implements Cloneable {

  public abstract String[] getCapabilities(ImapConnection conn);

  public abstract String[] getCommandIdentifier();

  /***
   * <p>Processes the imap lie prefixed by a command returned by getCommandIdentifier().</p>
   *
   * @param line           the line containing the command to be processed
   * @return               multilined server reply (if any)
   * @throws ImapException if processing fails
   */
  public abstract String[] processCommand(ImapLine line) throws ImapException;

}
