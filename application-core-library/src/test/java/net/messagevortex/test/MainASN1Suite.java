package net.messagevortex.test;


import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import net.messagevortex.asn1.AsymmetricKey;
import net.messagevortex.asn1.HeaderRequest;
import net.messagevortex.test.asn1.AbstractBlockTest;
import net.messagevortex.test.asn1.AsymmetricKeyReencodingTest;
import net.messagevortex.test.asn1.AsymmetricKeyTest;
import net.messagevortex.test.asn1.BlendingParameterTest;
import net.messagevortex.test.asn1.BuildSamplesTest;
import net.messagevortex.test.asn1.CipherSpecTest;
import net.messagevortex.test.asn1.FuzzerTest;
import net.messagevortex.test.asn1.HeaderRequestTest;
import net.messagevortex.test.asn1.IdentityBlockTest;
import net.messagevortex.test.asn1.IdentityStoreTest;
import net.messagevortex.test.asn1.MacAlgorithmTest;
import net.messagevortex.test.asn1.PaddingTest;
import net.messagevortex.test.asn1.SecurityLevelTest;
import net.messagevortex.test.asn1.SymmetricKeyTest;
import net.messagevortex.test.asn1.VortexMessageTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BuildSamplesTest.class,
        BlendingParameterTest.class,
        CipherSpecTest.class,
        FuzzerTest.class,
        VortexMessageTest.class,
        AbstractBlockTest.class,
        MacAlgorithmTest.class,
        SecurityLevelTest.class,
        SymmetricKeyTest.class,
        AsymmetricKeyTest.class,
        AsymmetricKeyReencodingTest.class,
        IdentityStoreTest.class,
        HeaderRequestTest.class,
        PaddingTest.class,
        IdentityBlockTest.class
})

/*
 * Test all classes related to parsing/handling ASN.1 data.
 */
public class MainASN1Suite {

  @Before
  public void init() {
    AsymmetricKey.setCacheFileName("AsymmetricKey.cache");
  }

  public static junit.framework.Test suite() {
    final TestSuite s = new TestSuite();
    s.addTest(new JUnit4TestAdapter(BuildSamplesTest.class));
    s.addTest(new JUnit4TestAdapter(BlendingParameterTest.class));
    s.addTest(new JUnit4TestAdapter(CipherSpecTest.class));
    s.addTest(new JUnit4TestAdapter(FuzzerTest.class));
    s.addTest(new JUnit4TestAdapter(VortexMessageTest.class));
    s.addTest(new JUnit4TestAdapter(AbstractBlockTest.class));
    s.addTest(new JUnit4TestAdapter(MacAlgorithmTest.class));
    s.addTest(new JUnit4TestAdapter(SecurityLevelTest.class));
    s.addTest(new JUnit4TestAdapter(SymmetricKeyTest.class));
    s.addTest(new JUnit4TestAdapter(AsymmetricKeyTest.class));
    s.addTest(new JUnit4TestAdapter(AsymmetricKeyReencodingTest.class));
    s.addTest(new JUnit4TestAdapter(IdentityStoreTest.class));
    s.addTest(new JUnit4TestAdapter(HeaderRequestTest.class));
    s.addTest(new JUnit4TestAdapter(PaddingTest.class));
    s.addTest(new JUnit4TestAdapter(IdentityBlockTest.class));
    return s;
  }

}
