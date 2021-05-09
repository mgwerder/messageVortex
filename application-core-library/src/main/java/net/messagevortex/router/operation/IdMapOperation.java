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

import net.messagevortex.MessageVortexLogger;

import java.io.Serializable;
import java.util.logging.Level;

/**
 * This is a dummy operation mainly for testing it maps output to input ids.
 */
public class IdMapOperation extends AbstractOperation implements Serializable {

  public static final long serialVersionUID = 100000000019L;

  private static final transient java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    //MessageVortexLogger.setGlobalLogLevel(Level.ALL);
  }

  private final int[] outputId;
  private final int[] inputId;

  /***
   * <p>Creates a simple operation mapping the input to the output ID.</p>
   *
   * @param sourceId  the first source ID to be mapped
   * @param targetId  the first target ID to be mapped
   * @param number    the number of subsequent blocks to be mapped
   */
  public IdMapOperation(int sourceId, int targetId, int number) {
    inputId = new int[number];
    outputId = new int[number];
    for (int i = 0; i < inputId.length; i++) {
      inputId[i] = sourceId + i;
      outputId[i] = targetId + i;
    }
  }

  @Override
  public int[] getOutputId() {
    return outputId.clone();
  }

  @Override
  public int[] getInputId() {
    return inputId.clone();
  }

  @Override
  public boolean canRun() {
    return true;
  }

  @Override
  public int[] execute(int[] id) {
    LOGGER.log(Level.INFO, "running IDMapper " + inputId[0] + "/" + outputId[0]
               + "/" + inputId.length);
    for (int i = 0; i < inputId.length; i++) {
      payload.setCalculatedPayload(outputId[i], payload.getPayload(inputId[i]));
    }
    return getOutputId();
  }

  @Override
  public String toString() {
    return inputId[0] + "->IdMapper(" + inputId.length + ")->" + outputId[0];
  }
}
