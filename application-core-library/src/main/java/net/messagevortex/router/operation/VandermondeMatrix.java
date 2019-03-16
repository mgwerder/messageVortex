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

/**
 * <p>Van der mode matrices.</p>
 *
 * <p>This class initializes a matrixContent with van der Monde values (F_{x,y}=y^x).</p>
 */
public class VandermondeMatrix extends Matrix {

  public VandermondeMatrix(VandermondeMatrix m) {
    super(m);
  }

  /***
   * <p>Creates a standardized Van Der Monde matrix for distributiong data among multiple nodes.</p>
   *
   * @param x the number of columns
   * @param y the number of rows
   * @param mode the math mode (should be a Gauloise Field to be effective
   */
  public VandermondeMatrix(int x, int y, MathMode mode) {
    super(x, y, mode);
    // init matrixContent with given math mode
    for (int yl = 0; yl < y; yl++) {
      setField(0, yl, 1);
      if (x > 1) {
        setField(1, yl, yl);
      }
    }
    if (x > 1) {
      for (int yl = 0; yl < y; yl++) {
        for (int xl = 2; xl < x; xl++) {
          setField(xl, yl, mode.mul(getField(xl - 1, yl), yl));
        }
      }
    }
  }

}
