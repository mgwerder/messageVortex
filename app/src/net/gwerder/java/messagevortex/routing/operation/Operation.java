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

import net.gwerder.java.messagevortex.asn1.IdentityBlock;

/**
 * This interface represents a standard operation in the payload space of an identity.
 */
public interface Operation {

    IdentityBlock getIdentity();

    /***
     * Checks if all prerequisiting fields do exist or can be provided by a subsequent operation.
     *
     * @return true if all prerequisits can potentially be satisfied
     */
    boolean canRun();

    /***
     * Checks if this operation is still valid or might be purged from the identities working space
     *
     * @return true if the operation should remain in the payload space
     */
    boolean isInUsagePeriod();

    /***
     * Gets all ids which are written by this operation.
     *
     * @return array representing all ids which will be potentially set by this operation
     */
    int[] getOutputID();

    /***
     * Gets all ids which are required to execute this operation.
     *
     * @return array representing all ids which will be potentially set by this operation
     */
    int[] getInputID();

    /***
     * Executes the operation and sets at least the provided set of id.
     *
     * this operation might trigger to execute prerequisiting operations.
     *
     * @param id the namespace id to be set minimally
     * @return array representing all ids which have been set
     */
    int[] execute(int[] id);

    /***
     * sets the internal payload and associated identity.
     *
     * This method is called from the InternalPayload when registering.
     *
     * @param payload the internal payload of an identity to be registered within
     */
    public void setInternalPayload(InternalPayload payload);

}
