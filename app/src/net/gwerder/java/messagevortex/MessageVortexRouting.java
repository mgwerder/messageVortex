package net.gwerder.java.messagevortex;
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

import net.gwerder.java.messagevortex.accounting.Accountant;
import net.gwerder.java.messagevortex.asn1.VortexMessage;
import net.gwerder.java.messagevortex.blending.BlenderReceiver;
import net.gwerder.java.messagevortex.routing.operation.RoutingSender;

/**
 * Created by Martin on 01.02.2018.
 */
public class MessageVortexRouting implements BlenderReceiver {

    private RoutingSender routingSender;
    private Accountant accountant;

    public MessageVortexRouting( Accountant accountant, RoutingSender routingSender ) {
        setRoutingSender( routingSender );
        setAccountant( accountant );
    }

    public final RoutingSender getRoutingSender() {
        return routingSender;
    }

    public final RoutingSender setRoutingSender( RoutingSender routingSender ) {
        RoutingSender ret = routingSender;
        this.routingSender = routingSender;
        return ret;
    }

    public final Accountant getAccountant() {
        return accountant;
    }

    public final Accountant setAccountant( Accountant accountant ) {
        Accountant ret = this.accountant;
        this.accountant = accountant;
        return ret;
    }

    @Override
    public boolean gotMessage( VortexMessage message ) {

        return false;
    }
}
