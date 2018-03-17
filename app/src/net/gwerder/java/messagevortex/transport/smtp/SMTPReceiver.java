package net.gwerder.java.messagevortex.transport.smtp;
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

import net.gwerder.java.messagevortex.transport.*;

import java.io.IOException;

/**
 * Created by martin.gwerder on 24.01.2018.
 */
public class SMTPReceiver implements SocketListener {

    private ListeningSocketChannel listener;

    public SMTPReceiver(int port, SecurityContext secContext, TransportReceiver receiver ) throws IOException {

    }

    @Override
    public void gotConnect(AbstractConnection ac) {
        // FIXME handle incomming connect
    }

    public TransportReceiver getTransportReceiver() {
        // FIXME this is a stub
        return null;
    }

    public TransportReceiver setTransportReceiver( TransportReceiver receiver ) {
        // FIXME this is a stub
        return null;
    }

    public void shutdown() {
        listener.shutdown();
    }

    public int getPort() {
        return listener.getPort();
    }
}
