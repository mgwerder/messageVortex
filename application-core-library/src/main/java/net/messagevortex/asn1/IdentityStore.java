package net.messagevortex.asn1;

import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.RunningDaemon;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.commandline.CommandLineHandlerIdentityStoreCreate;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.logging.Level;

/**
 * Stores all known identities of a node. Identities are stored as IdentityStoreBlocks.
 ***/
@CommandLine.Command(
        description = "Manipulator for IdentityStore",
        name = "intentitystore",
        aliases = {"store", "is"},
        subcommands = {
                CommandLineHandlerIdentityStoreCreate.class
        }
)
public class IdentityStore extends AbstractBlock
        implements Serializable, Callable<Integer>, RunningDaemon {
  
  public static final long serialVersionUID = 100000000008L;
  
  private static final java.util.logging.Logger LOGGER;
  
  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    //MessageVortexLogger.setGlobalLogLevel(Level.ALL);
  }
  
  private static IdentityStore demo = null;
  private AsymmetricKey hostIdentity = null;
  private final Map<String, IdentityStoreBlock> blocks = new TreeMap<>();
  
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
  
  /***
   * <p>Creates a new complete dummy identity store suitable for testing purposes.</p>
   *
   * @return returns a reference identity store
   * @throws IOException if building of the store fails
   */
  public static synchronized IdentityStore getIdentityStoreDemo() throws IOException {
    if (demo == null) {
      demo = getNewIdentityStoreDemo(true);
    }
    return demo;
  }
  
  /***
   * <p>Creates a new dummy identity store suitable for testing purposes.</p>
   *
   * <p>the identity store contains an own identity, one hundred
   * node idetities and 40 recipient identities.</p>
   *
   * @param complete if true the node and recipient identities contain the private key too
   * @return the requested identity store
   * @throws IOException if building of the store fails
   */
  public static IdentityStore getNewIdentityStoreDemo(boolean complete) throws IOException {
    IdentityStore tmp = new IdentityStore();
    // creating own identity
    tmp.add(IdentityStoreBlock.getIdentityStoreBlockDemo(
            IdentityStoreBlock.IdentityType.OWNED_IDENTITY, "user1@localhost", complete)
    );
    // creating ten dummy identities
    for (int i = 0; i < 10; i++) {
      tmp.add(IdentityStoreBlock.getIdentityStoreBlockDemo(
              IdentityStoreBlock.IdentityType.NODE_IDENTITY, "dummy" + i + "@localhost", complete)
      );
    }
    // creating nine recipient identities
    for (int i = 1; i < 10; i++) {
      tmp.add(IdentityStoreBlock.getIdentityStoreBlockDemo(
              IdentityStoreBlock.IdentityType.RECIPIENT_IDENTITY, "user" + i
                      + "@localhost", complete)
      );
    }
    return tmp;
  }
  
  /***
   * <p>Get the own identity key.</p>
   *
   * @return the requested key
   */
  public AsymmetricKey getHostIdentity() {
    return hostIdentity;
  }
  
  /***
   * <p>Sets the owned key.</p>
   *
   * @param identity identity key to be set as own key
   * @return the previously set key
   */
  public AsymmetricKey setHostIdentity(AsymmetricKey identity) {
    AsymmetricKey ret = hostIdentity;
    this.hostIdentity = identity;
    return ret;
  }
  
  public String[] getIdentityList() {
    return blocks.keySet().toArray(new String[0]);
  }
  
  /***
   * <p>Gets a random set of known recipient identities.</p>
   *
   * @param size the size of the anonymity set
   * @return the anonymity set
   * @throws IOException if requested anonymity set size is too big for this store
   */
  public Set<IdentityStoreBlock> getAnonSet(int size) throws IOException {
    LOGGER.log(Level.INFO, "Executing getAnonSet(" + size + ") from " + blocks.size());
    if (size > blocks.size() + 1) {
      // unable to create a block with a bigger anonymity set than the sum of identity store blocks
      return null;
    }
    Set<IdentityStoreBlock> ret = new HashSet<>();
    String[] keys = blocks.keySet().toArray(new String[0]);
    int i = 0;
    while (ret.size() < size && i < 10000) {
      i++;
      IdentityStoreBlock isb = blocks.get(keys[ExtendedSecureRandom.nextInt(keys.length)]);
      if (isb != null && (
              isb.getType() == IdentityStoreBlock.IdentityType.RECIPIENT_IDENTITY
                      || isb.getType() == IdentityStoreBlock.IdentityType.NODE_IDENTITY)
              && !ret.contains(isb)) {
        ret.add(isb);
        LOGGER.log(Level.FINER, "adding to anonSet " + isb.getNodeAddress());
      } else {
        if (isb != null) {
          LOGGER.log(Level.INFO, "Skipping " + isb.getNodeAddress() + " (" + isb.getType() + ")");
        } else {
          LOGGER.log(Level.SEVERE, "Skipping ISB==null");
        }
      }
    }
    if (ret.size() < size) {
      throw new IOException("unable to get anon set (size " + size + " too big; achieved: "
              + ret.size() + ")?");
    }
    LOGGER.log(Level.FINE, "done getAnonSet()");
    return ret;
  }

  protected final void parse(byte[] p) throws IOException {
    try (ASN1InputStream aIn = new ASN1InputStream(p)) {
      parse(aIn.readObject());
    }
  }
  
  protected final void parse(ASN1Encodable p) throws IOException {
    LOGGER.log(Level.FINER, "Executing parse()");
    
    ASN1Sequence s1 = ASN1Sequence.getInstance(p);
    for (ASN1Encodable ao : s1.toArray()) {
      IdentityStoreBlock sb = new IdentityStoreBlock(ao);
      add(sb);
    }
    LOGGER.log(Level.FINER, "Finished parse()");
  }
  
  /***
   * <p>Adds an existing identity store block to the store.</p>
   *
   * @param isb the block to be added
   */
  public void add(IdentityStoreBlock isb) {
    String ident = "";
    if (isb.getIdentityKey() != null) {
      ident = toHex(isb.getIdentityKey().getPublicKey());
    }
    blocks.put(ident, isb);
  }
  
  /***
   * <p>removes a node address from the identity store.</p>
   *
   * @param nodeAddress the node addrress to be removed
   * @throws IOException if node address is not contained in identity store
   */
  public void removeAddress(String nodeAddress) throws IOException {
    List<String> rem = new ArrayList<>();
    if (nodeAddress != null) {
      nodeAddress = nodeAddress.toLowerCase();
    }
    synchronized (blocks) {
      for (Map.Entry<String, IdentityStoreBlock> e : blocks.entrySet()) {
        String v = e.getValue().getNodeAddress();
        if ((v == null && nodeAddress == null)
                || (v != null
                && v.toLowerCase().equals(nodeAddress))) {
          rem.add(e.getKey());
        }
      }
      if (rem.size() == 0) {
        throw new IOException("unable to find nodeAddress \"" + nodeAddress + "\"");
      }
      for (String s : rem) {
        blocks.remove(s);
      }
    }
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
  
  public IdentityStoreBlock getIdentity(String id) {
    return blocks.get(id);
  }
  
  public Integer call() {
    return null;
  }
  
  @Override
  public void startDaemon() {
    // FIXME implement file monitor
  }
  
  @Override
  public void stopDaemon() {
    // FIXME implement file monitor
  }
  
  @Override
  public void shutdownDaemon() {
    // FIXME implement file monitor
  }
}
