package net.messagevortex.asn1.encryption;

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

import org.bouncycastle.asn1.ASN1Enumerated;

/**
 * This enum maps the ASN1 error coders as defined in StatusCode.
 */
public enum StatusCode {
  // system messages (no failure)
  OK(2000),
  TRANSFER_QUOTA_STATUS(2101),
  MESSAGE_QUOTA_STATUS(2102),
  PUZZLE_REQUIRED(2201),

  // protocol usage failures
  TRANSFER_QUOTA_EXCEEDED(3001),
  MESSAGE_QUOTA_EXCEEDED(3002),
  IDENTITY_UNKNOWN(3101),
  MESSAGE_CHUNK_MISSING(3201),
  MESSAGE_LIFE_EXPIRED(3202),
  PUZZLE_UNKNOWN(3301),

  // capability errors
  MAC_ALGORITHM_UNKNOWN(3801),
  SYMMETRIC_ALGORITHM_UNKNOWN(3802),
  ASYMMETRIC_ALGORITHM_UNKNOWN(3803),
  PRNG_ALGORITHM_UNKNOWN(3804),
  MISSING_PARAMETERS(3820),
  BAD_PARAMETERS(3821),
  HOST_ERROR(5001);

  private final int id;
  private final ASN1Enumerated asn;

  StatusCode(int i) {
    id = i;
    asn = new ASN1Enumerated(id);
  }

  /***
   * <p>Gets the ASN1 constant for this status code.</p>
   *
   * @return the requested constant for ASN1 encoding
   */
  public int getId() {
    return id;
  }

  /***
   * <p>Returns the corresponding ASN1 enumeration.</p>
   *
   * @return the ASN1 enumeration representing this status
   */
  public ASN1Enumerated toAsn1() {
    return asn;
  }

}
