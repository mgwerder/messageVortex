package net.gwerder.java.messagevortex.transport.imap;
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

import javax.security.auth.callback.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImapCommandAuthenticateCallbackHandler implements CallbackHandler {

    private String username;
    private String password;

    public ImapCommandAuthenticateCallbackHandler( String username, String password ) {
        this.username=username;
        this.password=password;
    }

    private static final Logger LOGGER;
    static {
        LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof TextOutputCallback) {
                // display the message according to the specified type
                Level l;
                TextOutputCallback toc = (TextOutputCallback)callbacks[i];
                switch (toc.getMessageType()) {
                    case TextOutputCallback.INFORMATION:
                        l=Level.INFO;
                        break;
                    case TextOutputCallback.ERROR:
                        l=Level.SEVERE;
                        break;
                    case TextOutputCallback.WARNING:
                        l=Level.WARNING;
                        break;
                    default:
                        throw new IOException("Unsupported message type: " + toc.getMessageType());
                }

                LOGGER.log( l, "SASL message: " + toc.getMessage() );
            } else if (callbacks[i] instanceof NameCallback) {
                // provide username
                NameCallback nc = (NameCallback)callbacks[i];
                nc.setName(username);
            } else if (callbacks[i] instanceof PasswordCallback) {
                // provide password
                PasswordCallback pc = (PasswordCallback)callbacks[i];
                pc.setPassword( password.toCharArray() );
            } else {
                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
            }
        }
    }

    // AuthorizeCallback
    // NameCallback
    // PasswordCallback
    // RealmCallback
    // RealmChoiceCallback

}
