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

public interface MathMode {

  /***
   * <p>Multiplys  c1 ith c2.</p>
   *
   * @param c1 the first operand
   * @param c2 the second operand
   * @return the result of the multiplication
   */
  int mul(int c1, int c2);

  /***
   * <p>Divides c1 by c2 (without remainder).</p>
   *
   * @param c1 the dividend
   * @param c2 the divisor
   * @return the result of the division
   */
  int div(int c1, int c2);

  /***
   * <p>Add c1 with c2.</p>
   *
   * @param c1 the first operand
   * @param c2 the second operand
   * @return the result of the addition
   */
  int add(int c1, int c2);

  /***
   * <p>Subtract c2 from c1.</p>
   *
   * @param c1 the base value
   * @param c2 the the value to subtract from the base value
   * @return the result of the subtraction
   */
  int sub(int c1, int c2);

  /***
   * <p>get the identitfication representation.</p>
   *
   * @return th identification string
   */
  String toString();

}
