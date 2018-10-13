package net.messagevortex.asn1;

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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;

/**
 * Stores all known identities of a node. Identities are stored as IdentityStoreBlocks.
 ***/
public class IdentityStore extends AbstractBlock implements Serializable {

  public static final long serialVersionUID = 100000000008L;

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    MessageVortexLogger.setGlobalLogLevel(Level.ALL);
  }

  private static IdentityStore demo = null;
  private AsymmetricKey hostIdentity = null;
  private Map<String, IdentityStoreBlock> blocks = new TreeMap<>();

  public IdentityStore() {
    blocks.clear();
  }

  public IdentityStore(byte[] b) throws IOException {
    this();
    parse(b);
  }

  /***
   * <p>Create object from ASN.1 encoded file.</p>
   *
   * @param f the file to be parsed
   * @throws IOException if parsing of ASN.1 code fails
   */
  public IdentityStore(File f) throws IOException {
    this();
    Path asn1DataPath = Paths.get(f.getAbsolutePath());
    byte[] p = Files.readAllBytes(asn1DataPath);
    parse(p);
  }

  public static void resetDemo() {
    demo = null;
  }

  public static IdentityStore getIdentityStoreDemo() throws IOException {
    if (demo == null) {
      demo = getNewIdentityStoreDemo(true);
    }
    return demo;
  }

  public static IdentityStore getNewIdentityStoreDemo(boolean complete) throws IOException {
    IdentityStore tmp = new IdentityStore();
    tmp.add(IdentityStoreBlock.getIdentityStoreBlockDemo(
            IdentityStoreBlock.IdentityType.OWNED_IDENTITY, complete)
    );
    for (int i = 0; i < 100; i++) {
      tmp.add(IdentityStoreBlock.getIdentityStoreBlockDemo(
              IdentityStoreBlock.IdentityType.NODE_IDENTITY, complete)
      );
    }
    for (int i = 0; i < 40; i++) {
      tmp.add(IdentityStoreBlock.getIdentityStoreBlockDemo(
              IdentityStoreBlock.IdentityType.RECIPIENT_IDENTITY, complete)
      );
    }
    return tmp;
  }

  public AsymmetricKey getHostIdentity() {
    return hostIdentity;
  }

  public AsymmetricKey setHostIdentity(AsymmetricKey identity) {
    AsymmetricKey ret = hostIdentity;
    this.hostIdentity = identity;
    return ret;
  }

  public List<IdentityStoreBlock> getAnonSet(int size) throws IOException {
    LOGGER.log(Level.FINE, "Executing getAnonSet(" + size + ") from " + blocks.size());
    List<IdentityStoreBlock> ret = new ArrayList<>();
    String[] keys = blocks.keySet().toArray(new String[0]);
    int i = 0;
    while (ret.size() < size && i < 10000) {
      i++;
      IdentityStoreBlock isb = blocks.get(keys[ExtendedSecureRandom.nextInt(keys.length)]);
      if (isb != null && isb.getType() == IdentityStoreBlock.IdentityType.RECIPIENT_IDENTITY
              && !ret.contains(isb)) {
        ret.add(isb);
        LOGGER.log(Level.FINER, "adding to anonSet "
                + Arrays.hashCode(isb.getIdentityKey().getPublicKey()));
      }
    }
    if (ret.size() < size) {
      throw new IOException("unable to get anon set (size " + size + " too big; achieved: "
              + ret.size() + ")?");
    }
    LOGGER.log(Level.FINE, "done getAnonSet()");
    return ret;
  }

  protected void parse(byte[] p) throws IOException {
    try (ASN1InputStream aIn = new ASN1InputStream(p)) {
      parse(aIn.readObject());
    }
  }

  protected void parse(ASN1Encodable p) throws IOException {
    LOGGER.log(Level.FINER, "Executing parse()");

    ASN1Sequence s1 = ASN1Sequence.getInstance(p);
    for (ASN1Encodable ao : s1.toArray()) {
      IdentityStoreBlock sb = new IdentityStoreBlock(ao);
      add(sb);
    }
    LOGGER.log(Level.FINER, "Finished parse()");
  }

  public void add(IdentityStoreBlock isb) {
    String ident = "";
    if (isb.getIdentityKey() != null) {
      ident = toHex(isb.getIdentityKey().getPublicKey());
    }
    blocks.put(ident, isb);
  }

  @Override
  public ASN1Object toAsn1Object(DumpType dumpType) throws IOException {
    // Prepare encoding
    LOGGER.log(Level.FINER, "Executing toAsn1Object()");

    ASN1EncodableVector v = new ASN1EncodableVector();

    LOGGER.log(Level.FINER, "adding blocks");
    for (Map.Entry<String, IdentityStoreBlock> e : blocks.entrySet()) {
      v.add(e.getValue().toAsn1Object(dumpType));
    }
    ASN1Sequence seq = new DERSequence(v);
    LOGGER.log(Level.FINER, "done toAsn1Object()");
    return seq;
  }

  @Override
  public String dumpValueNotation(String prefix, DumpType dumpType) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append(prefix).append("i IdentityStore ::= {").append(CRLF);
    sb.append(prefix).append("  identities {").append(CRLF);
    int i = 0;
    for (Map.Entry<String, IdentityStoreBlock> e : blocks.entrySet()) {
      if (i > 0) {
        sb.append(',').append(CRLF);
      }
      sb.append(prefix).append("    -- Dumping IdentityBlock ").append(e.getKey()).append(CRLF);
      sb.append(prefix).append("    ")
              .append(e.getValue().dumpValueNotation(prefix + "    ", dumpType));
      i++;
    }
    sb.append(CRLF);
    sb.append(prefix).append("  }").append(CRLF);
    sb.append(prefix).append('}').append(CRLF);
    return sb.toString();
  }

  public static void main(String[] args) throws Exception {
    LOGGER.log(Level.INFO, "\n;;; -------------------------------------------");
    LOGGER.log(Level.INFO, ";;; creating blank dump");
    IdentityStore m = new IdentityStore();
    LOGGER.log(Level.INFO, m.dumpValueNotation("", DumpType.ALL_UNENCRYPTED));

    LOGGER.log(Level.INFO, "\n;;; -------------------------------------------");
    LOGGER.log(Level.INFO, ";;; Demo Store Test");
    IdentityStore m2 = IdentityStore.getIdentityStoreDemo();
    LOGGER.log(Level.INFO, ";;; dumping\r\n" + m2.dumpValueNotation("", DumpType.ALL_UNENCRYPTED));
    LOGGER.log(Level.INFO, ";;; reencode check");
    LOGGER.log(Level.INFO, ";;;   getting DER stream");
    String tmpDir = System.getProperty("java.io.tmpdir");
    LOGGER.log(Level.INFO, ";;;   storing to DER stream to " + tmpDir);
    DEROutputStream f = new DEROutputStream(Files.newOutputStream(Paths.get(tmpDir + "/temp.der")));
    f.writeObject(m.toAsn1Object(DumpType.ALL_UNENCRYPTED));
    f.close();
    byte[] b1 = m.toBytes(DumpType.ALL_UNENCRYPTED);
    LOGGER.log(Level.INFO, ";;;   parsing DER stream");
    IdentityStore m3 = new IdentityStore(b1);
    LOGGER.log(Level.INFO, ";;;   getting DER stream again");
    byte[] b2 = m3.toBytes(DumpType.ALL_UNENCRYPTED);
    LOGGER.log(Level.INFO, ";;;   comparing");
    if (Arrays.equals(b1, b2)) {
      LOGGER.log(Level.INFO, "Reencode success");
    } else {
      LOGGER.log(Level.INFO, "Reencode FAILED");
    }
  }

}
