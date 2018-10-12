package net.gwerder.java.messagevortex.transport;

public enum SaslMechanisms {
  DIGEST_MD5("DIGEST-MD5", 32),
  CRAM_MD5("CRAM-MD5", 16),
  PLAIN("PLAIN", 0);

  String value;
  int strength;

  SaslMechanisms(String value, int strength) {
    this.value = value;
    this.strength = strength;
  }

  @Override
  public String toString() {
    return this.value;
  }

  public int getStrength() {
    return strength;
  }
}
