package net.messagevortex.test.asn1;

import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.asn1.HeaderRequestIdentity;
import net.messagevortex.asn1.HeaderRequestIncreaseMessageQuota;
import net.messagevortex.asn1.HeaderRequestIncreaseTransferQuota;
import net.messagevortex.asn1.UsagePeriod;
import net.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class HeaderRequestTest {

  @Test
  public void IncreaseMessageQuotaTest() throws Exception {
    HeaderRequestIncreaseMessageQuota imq = new HeaderRequestIncreaseMessageQuota();
    long quota = ExtendedSecureRandom.nextInt(65537*32000);
    imq.setQuota(quota);
    ASN1Encodable enc = ASN1TaggedObject.getInstance(imq.toBytes(DumpType.ALL)).getObject();
    HeaderRequestIncreaseMessageQuota imq2 = new HeaderRequestIncreaseMessageQuota( enc );
    assertTrue("reencoded is not equal in dump", imq.dumpValueNotation("",DumpType.ALL_UNENCRYPTED).equals(imq2.dumpValueNotation("",DumpType.ALL_UNENCRYPTED)));
    assertTrue( "quota changed unexpectedly",imq2.getQuota() == imq.getQuota());
  }

  @Test
  public void IncreaseTransferQuotaTest() throws Exception {
    HeaderRequestIncreaseTransferQuota itq = new HeaderRequestIncreaseTransferQuota();
    long quota = ExtendedSecureRandom.nextInt(65537*32000);
    itq.setQuota(quota);
    ASN1Encodable enc = ASN1TaggedObject.getInstance(itq.toBytes(DumpType.ALL)).getObject();
    HeaderRequestIncreaseMessageQuota itq2 = new HeaderRequestIncreaseMessageQuota( enc );
    assertTrue("reencoded is not equal in dump", itq.dumpValueNotation("",DumpType.ALL_UNENCRYPTED).equals(itq2.dumpValueNotation("",DumpType.ALL_UNENCRYPTED)));
    assertTrue( "quota changed unexpectedly",itq2.getQuota() == itq.getQuota());
  }

  @Test
  public void requestIdentityTest() throws Exception {
    HeaderRequestIdentity itq = new HeaderRequestIdentity();
    UsagePeriod period = new UsagePeriod();
    itq.setUsagePeriod(period);
    ASN1Encodable enc = ASN1TaggedObject.getInstance(itq.toBytes(DumpType.ALL)).getObject();
    HeaderRequestIdentity itq2 = new HeaderRequestIdentity( enc );
    System.out.println("source " + itq.dumpValueNotation("", DumpType.ALL_UNENCRYPTED));
    System.out.println("target " + itq2.dumpValueNotation("", DumpType.ALL_UNENCRYPTED));
    assertTrue("reencoded is not equal in dump", itq.dumpValueNotation("",DumpType.ALL_UNENCRYPTED).equals(itq2.dumpValueNotation("",DumpType.ALL_UNENCRYPTED)));
    assertTrue( "usage period changed unexpectedly",itq2.getUsagePeriod().compareTo( itq.getUsagePeriod() ) == 0);
  }

}
