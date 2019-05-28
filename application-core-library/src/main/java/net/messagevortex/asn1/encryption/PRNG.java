package net.messagevortex.asn1.encryption;

public interface PRNG {

  /***
   * <p>Returns the next random byte of the initialized PRNG.</p>
   *
   * @return the requested byte
   */
  byte nextByte();

  /***
   * <p>Resets the prng to the last specified seed.</p>
   */
  void reset();

}
