package net.gwerder.java.messagevortex.transport;

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

import java.util.HashMap;
import java.util.Map;

import net.gwerder.java.messagevortex.transport.imap.ImapConnection;

public class AuthenticationProxy {

  private ImapConnection conn = null;

  public ImapConnection setImapConnection(ImapConnection conn) {
    ImapConnection oc = this.conn;
    this.conn = conn;
    return oc;
  }

  /***
   * <p>Get the ImapConnection object which belongs to this proxy.</p>
   *
   * @return A Connection object which is connected to this proxy
   */
  public ImapConnection getImapConnection() {
    return this.conn;
  }


  private final Map<String, Credentials> users = new HashMap<>();

  public void addUser(String username, String password) {
    users.put(username.toLowerCase(), new Credentials(username, password));
  }

  public void addCredentials(Credentials creds) {
    users.put(creds.getUsername().toLowerCase(), creds);
  }

  public boolean login(String username, String password) {
    // Always require a username or password
    if (username == null || password == null) {
      return false;
    }

    // check if user exists
    if (users.get(username.toLowerCase()) == null) {
      return false;
    }

    // check if password is correct
    return users.get(username.toLowerCase()).getPassword().equals(password);
  }

  public Credentials getCredentials(String authzid) {
    return users.get(authzid);
  }

}
