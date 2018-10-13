package net.messagevortex.transport;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DummyTransportSender implements TransportSender {

  static final Map<String, TransportReceiver> endpoints = new ConcurrentHashMap<>();

  public DummyTransportSender(String id, TransportReceiver blender) throws IOException {
    synchronized (endpoints) {
      if (endpoints.containsKey(id)) {
        throw new IOException("Duplicate transport endpoint identifier (" + id + ")");
      }
      endpoints.put(id, blender);
    }
  }

  public DummyTransportSender(TransportReceiver blender) {
    synchronized (endpoints) {
      String id = null;
      while (id == null || endpoints.containsKey(id)) {
        id = RandomString.nextString(5, "0123456789abcdef@example.com");
      }
      endpoints.put(id, blender);
    }
  }

  public void sendMessage(final String address, InputStream is) throws IOException {
    if (address == null || endpoints.get(address) == null) {
      throw new IOException("recipient address is unknown");
    }
    // convert is to byte array
    ByteArrayBuilder bab = new ByteArrayBuilder();
    int n;
    byte[] buffer = new byte[1024];
    while ((n = is.read(buffer)) > -1) {
      bab.append(buffer, n);
    }

    // send byte array as input stream to target
    final InputStream iso = new ByteArrayInputStream(bab.toBytes());
    new Thread() {

      @Override
      public void run() {
        endpoints.get(address).gotMessage(iso);
      }
    }.start();
  }

}
