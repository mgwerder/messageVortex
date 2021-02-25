package net.messagevortex.router.operation;

/**
 * <p>Shifts bits circularly.</p>
 */
public class BitShifter {

  /***
   * <p>shifts bits circularly right.</p>
   *
   * @param value   the value to be shifted
   * @param shift   the number of positions to be shifted
   * @param length  the length of the circular buffer
   * @return the new value
   */
  public static int rshift(int value, int shift, byte length) {
    return lshift(value, -shift, length);
  }

  /***
   * <p>shifts bits circularly left.</p>
   *
   * @param value   the value to be shifted
   * @param shift   the number of positions to be shifted
   * @param length  the length of the circular buffer
   * @return the new value
   */
  public static int lshift(int value, int shift, byte length) {
    long ret = value;
    if (shift == 0) {
      return value;
    }
    int lshift = shift % length;
    if (lshift < 0) {
      lshift += length;
    }

    // do shift
    ret = ret << lshift;

    // move overflow to lower end
    long bitmask = ((long) Math.pow(2, lshift) - 1) << length;
    long lowbits = (ret & bitmask) >> length;
    ret = ret | lowbits;

    // truncate result (inefficient but works)
    ret = ret & ((int) Math.pow(2, length) - 1);
    return (int) ret;
  }

}
