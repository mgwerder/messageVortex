package net.messagevortex.router.operation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Offers galoise Math required for redundancy matrices.
 */
public class GaloisFieldMathMode implements MathMode {

  private final int gfFieldSize;
  private final int omega;
  private final int[] gfLog;
  private final int[] gfInverseLog;

  /** static list created with matlab primpoly(N,'max','nodisplay') **/
  static final int[] PRIM_POLYNOM = new int[] {
         3,    7,   11,   19,   37,    67,   131,      285,
       529, 1033, 2053, 4179, 8219, 17475, 32771,    65581,
          0,   0,    0,    0,    0,     0,     0, 16777351
  };

  static final Map<Integer, GaloisFieldMathMode> cachedMathMode = new LinkedHashMap<>();

  public GaloisFieldMathMode(int omega) {
    if (omega < 2 || omega > 32 || PRIM_POLYNOM[omega-1]==0 ) {
      throw new ArithmeticException("illegal GF size " + omega + " (PRIM_POLYNOM unknown)");
    }
    this.omega = omega;
    gfFieldSize = (int) Math.pow(2, omega);
    gfLog = new int[gfFieldSize];
    gfInverseLog = new int[gfFieldSize];
    int b = 1;
    for (int log = 0; log < gfFieldSize - 1; log++) {
      gfLog[b % gfFieldSize] = log;
      gfInverseLog[log % gfFieldSize] = b;
      b = BitShifter.lshift(b, 1, (byte) 33);
      if ((b & gfFieldSize) != 0) {
        b = b ^ PRIM_POLYNOM[omega - 1];
      }
    }
    // initialize undefined values with 0
    gfLog[0] = -1;
    gfInverseLog[gfFieldSize - 1] = -1;
  }

  /***
   * <p>Gets a singleton math mode for the specified omega.</p>
   *
   * @param omega  the number of bits to be used
   * @return the math mode (singleton)
   */
  public static GaloisFieldMathMode getGaloisFieldMathMode(int omega) {
    if (omega < 1 || omega > 16) {
      throw new IllegalArgumentException("omega (" + omega + ") out of range 1..16");
    }
    GaloisFieldMathMode ret = cachedMathMode.get(omega);
    if (ret == null) {
      ret = new GaloisFieldMathMode(omega);
      while(cachedMathMode.size()>50) {
        cachedMathMode.remove(cachedMathMode.keySet().iterator().next());
      }
      cachedMathMode.put(omega, ret);
    }
    return ret;
  }

  @Override
  public int mul(int c1, int c2) {
    if (c1 == 0 || c2 == 0) {
      return 0;
    }
    int sumLog = gfLog[c1] + gfLog[c2];
    if (sumLog >= gfFieldSize - 1) {
      sumLog -= gfFieldSize - 1;
    }
    return gfInverseLog[sumLog];
  }

  @Override
  public int div(int c1, int divisor) {
    if (c1 == 0) {
      return 0;
    }
    if (divisor == 0) {
      throw new ArithmeticException("Divisionby 0");
    }
    int diffLog = gfLog[c1] - gfLog[divisor];
    while (diffLog < 0) {
      diffLog += gfFieldSize - 1;
    }
    return gfInverseLog[diffLog];
  }

  @Override
  public int add(int c1, int c2) {
    return c1 ^ c2;
  }

  @Override
  public int sub(int c1, int c2) {
    return add(c1, c2);
  }

  public int[] getGfLog() {
    return gfLog.clone();
  }

  public int[] getGfIlog() {
    return gfInverseLog.clone();
  }

  @Override
  public String toString() {
    return "GF(2^" + omega + ")";
  }

  /**
   * <p>dumps transformation table of GF-Field.</p>
   *
   * @return returns a string representing the current table
   */
  public String getTableDump() {
    StringBuilder sb =new StringBuilder();
    sb.append("omega=").append( omega ).append( System.lineSeparator());
    sb.append("Add:xor; sub=xor; ").append( System.lineSeparator());
    sb.append("mul=iif (c1 == 0 || c2 == 0;0; gfilog[gfLog[c1] + gfLog[c2]-iif(gfLog[c1] + ")
        .append("gfLog[c2]>2^").append( omega ).append( "-1;2^\"+omega+\"-1;0)]").append(System.lineSeparator());
    sb.append("div=iif (c1 == 0;0;iif(c2==0;illegal;gfilog[gfLog[c1] - gfLog[c2]+iif(")
        .append("gfLog[c1] - gfLog[c2]<>0;2^\"+omega+\"-1;0))]").append( System.lineSeparator());
    sb.append(System.lineSeparator());

    int cols = (int) (Math.ceil(Math.sqrt(Math.pow(2, omega)) / 2));
    for (int x = 0; x < cols; x++) {
      sb.append("| num | log   | ilog  |  ");
    }
    sb.append(System.lineSeparator());
    for (int x = 0; x < cols; x++) {
      sb.append("+-----+-------+-------+  ");
    }
    sb.append(System.lineSeparator());
    int rows = (int) (Math.ceil(Math.pow(2, omega) / cols));
    for (int y = 0; y < rows; y++) {
      for (int x = 0; x < cols; x++) {
        int i = x * rows + y;
        if (i < Math.pow(2, omega)) {
          sb.append(String.format("| %3d | %5d | %5d |  ", i, gfLog[i], gfInverseLog[i]));
        }
      }
      sb.append(System.lineSeparator());
    }
    return sb.toString();
  }

}
