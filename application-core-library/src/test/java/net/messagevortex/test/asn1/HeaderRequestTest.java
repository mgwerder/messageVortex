package net.messagevortex.test.asn1;

import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.asn1.HeaderRequestIdentity;
import net.messagevortex.asn1.HeaderRequestIncreaseMessageQuota;
import net.messagevortex.asn1.HeaderRequestIncreaseTransferQuota;
import net.messagevortex.asn1.UsagePeriod;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.test.GlobalJunitExtension;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GlobalJunitExtension.class)
public class HeaderRequestTest {

  @Test
  public void IncreaseMessageQuotaTest() throws Exception {
    HeaderRequestIncreaseMessageQuota imq = new HeaderRequestIncreaseMessageQuota();
    long quota = ExtendedSecureRandom.nextInt(65537*32000);
    imq.setQuota(quota);
    ASN1Encodable enc = ASN1TaggedObject.getInstance(imq.toBytes(DumpType.ALL)).getObject();
    HeaderRequestIncreaseMessageQuota imq2 = new HeaderRequestIncreaseMessageQuota( enc );
    Assertions.assertTrue(imq.dumpValueNotation("",DumpType.ALL_UNENCRYPTED).equals(imq2.dumpValueNotation("",DumpType.ALL_UNENCRYPTED)), "reencoded is not equal in dump");
    Assertions.assertTrue(imq2.getQuota() == imq.getQuota(), "quota changed unexpectedly");
  }

  @Test
  public void IncreaseTransferQuotaTest() throws Exception {
    HeaderRequestIncreaseTransferQuota itq = new HeaderRequestIncreaseTransferQuota();
    long quota = ExtendedSecureRandom.nextInt(65537*32000);
    itq.setQuota(quota);
    ASN1Encodable enc = ASN1TaggedObject.getInstance(itq.toBytes(DumpType.ALL)).getObject();
    HeaderRequestIncreaseMessageQuota itq2 = new HeaderRequestIncreaseMessageQuota( enc );
    Assertions.assertTrue(itq.dumpValueNotation("",DumpType.ALL_UNENCRYPTED).equals(itq2.dumpValueNotation("",DumpType.ALL_UNENCRYPTED)), "reencoded is not equal in dump");
    Assertions.assertTrue(itq2.getQuota() == itq.getQuota(), "quota changed unexpectedly");
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
    Assertions.assertTrue(itq.dumpValueNotation("",DumpType.ALL_UNENCRYPTED).equals(itq2.dumpValueNotation("",DumpType.ALL_UNENCRYPTED)), "reencoded is not equal in dump");
    Assertions.assertTrue(itq2.getUsagePeriod().compareTo( itq.getUsagePeriod() ) == 0, "usage period changed unexpectedly");
  }

}
