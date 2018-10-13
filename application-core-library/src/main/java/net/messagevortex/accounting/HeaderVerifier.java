package net.messagevortex.accounting;

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
 * <p>Interface for an Accountant to verify the header for further processing.</p>
 */
public interface HeaderVerifier {

  /***
   * <p>checks the given IdentityBlock for validity of processing.</p>
   *
   * </p>One of the following criteria must be met:
   * <ul>
   *   <li>The identity is known and the serial has not yet reached its replay limit and is not
   *   replayed too early</li>
   *   <li>The identity is not known but has a RequestIdentityBlock</li>
   *   <li>The IdentityBlock is not known but has a request capability block</li>
   * </ul>
   * </p>
   *
   * @param header the header to be verified
   * @return the maximum nuber of bytes allowed for processing
   */
  int verifyHeaderForProcessing(IdentityBlock header);

}