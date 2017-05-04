package net.gwerder.java.messagevortex.routing.operation;
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

import net.gwerder.java.messagevortex.asn1.AbstractRedundancyOperation;
import net.gwerder.java.messagevortex.asn1.AddRedundancyOperation;
import net.gwerder.java.messagevortex.asn1.IdentityBlock;

public class AddRedundancy extends AbstractOperation {

    AbstractRedundancyOperation operation;

    public AddRedundancy(InternalPayload p, AddRedundancyOperation op) {
        super(p);
        this.operation=op;
        initDone();
    }

    @Override
    public boolean canRun() {
        return false;
    }

    @Override
    public int[] getOutputID() {
        int[] ret=new int[operation.getDataStripes()+operation.getRedundancy()];
        for(int i=0;i<ret.length;i++) {
            ret[i]=operation.getOutputId()+i;
        }
        return ret;
    }

    @Override
    public int[] getInputID() {
        int[] ret=new int[operation.getDataStripes()];
        for(int i=0;i<ret.length;i++) {
            ret[i]=operation.getInputId()+i;
        }
        return ret;
    }

    public int[] execute(int[] id) {
        int[] ret=getOutputID();
        // FIXME do redundancy calculation
        return ret;
    }

}
