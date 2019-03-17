package net.messagevortex.blender;

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
import net.messagevortex.asn1.BlendingSpec;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.VortexMessage;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.transport.Transport;
import net.messagevortex.transport.TransportReceiver;
import net.messagevortex.transport.dummy.DummyTransportTrx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public class DummyBlender extends Blender implements TransportReceiver {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    MessageVortexLogger.setGlobalLogLevel(Level.ALL);
  }

  String identity;
  Transport transport;
  BlendingReceiver router;
  IdentityStore identityStore;

  public DummyBlender(String section) throws IOException {
    // FIXME this is a duimmy constructor which breaks the implementation
    this("", null,new IdentityStore());
  }

  /***
   * <p>Creates a passthru blender which abstracts a local transport media.</p>
   *
   * @param identity        the identity (receiver/sender address)
   * @param router          the router layer to be used
   * @param identityStore   the identity store to be used (for decryption of headers)
   * @throws IOException    if anything fails :-D
   */
  public DummyBlender(String identity, BlendingReceiver router, IdentityStore identityStore)
          throws IOException {
    super(router, null);
    this.identity = identity;
    this.transport = new DummyTransportTrx(identity, this);
    this.router = router;
    if (identityStore == null) {
      throw new NullPointerException("identitystore may not be null");
    }
    this.identityStore = identityStore;
  }

  @Override
  public String getBlendingAddress() {
    return this.identity;
  }

  @Override
  public boolean blendMessage(BlendingSpec target, VortexMessage msg) {
    // encode message in clear readable and send it
    try {
      transport.sendMessage(target.getRecipientAddress(),
              new ByteArrayInputStream(msg.toBytes(DumpType.PUBLIC_ONLY)));
      return true;
    } catch (IOException ioe) {
      LOGGER.log(Level.SEVERE, "Unable to send to transport endpoint "
              + target.getRecipientAddress(), ioe);
      return false;
    }
  }

  @Override
  public boolean gotMessage(InputStream is) {
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      int numBytesRead;
      byte[] data = new byte[16384];
      while ((numBytesRead = is.read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, numBytesRead);
      }
      buffer.flush();
      return router.gotMessage(new VortexMessage(buffer.toByteArray(),
              identityStore.getHostIdentity()));
    } catch (IOException ioe) {
      return false;
    }
  }

  public void shutdownDaemon() {}
  public void startDaemon() {}
  public void stopDaemon() {}
}
