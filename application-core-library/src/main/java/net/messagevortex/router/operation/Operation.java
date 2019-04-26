package net.messagevortex.router.operation;

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

import net.messagevortex.asn1.IdentityBlock;

/**
 * <p>This interface represents a standard operation in the payload space of an identity.</p>
 */
public interface Operation {

  IdentityBlock getIdentity();

  /***
   * <p>Checks if all prerequisiting fields do exist or can be provided by a subsequent
   * operation.</p>
   *
   * @return true if all prerequisits can potentially be satisfied
   */
  boolean canRun();

  /***
   * <p>Checks if this operation is still valid or might be purged from the identities working
   * space.</p>
   *
   * @return true if the operation should remain in the payload space
   */
  boolean isInUsagePeriod();

  /***
   * <p>Gets all ids which are written by this operation.</p>
   *
   * @return array representing all ids which will be potentially set by this operation
   */
  int[] getOutputId();

  /***
   * <p>Gets all ids which are required to execute this operation.</p>
   *
   * @return array representing all ids which will be potentially set by this operation
   */
  int[] getInputId();

  /***
   * <p>Executes the operation and sets at least the provided set of id.</p>
   *
   * <p>This operation might trigger to execute prerequisiting operations.</p>
   *
   * @param id the namespace id to be set minimally
   * @return array representing all ids which have been set
   */
  int[] execute(int[] id);

  /***
   * <p>Sets the internal payload and associated identity.</p>
   *
   * <p>This method is called from the InternalPayloadSpace when registering.</p>
   *
   * @param payload the internal payload of an identity to be registered within
   */
  void setInternalPayload(InternalPayloadSpace payload);

}
