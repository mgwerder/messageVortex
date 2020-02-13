package net.messagevortex.test;

/**
 * Test all classes related to the CLI.
 */

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import net.messagevortex.asn1.AsymmetricKey;
import net.messagevortex.test.cli.GeneralCommandline;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        GeneralCommandline.class
})

public class MainCliSuite {

  @Before
  public void init() {
    AsymmetricKey.setCacheFileName("AsymmetricKey.cache");
  }

  public static junit.framework.Test suite() {
    final TestSuite s = new TestSuite();
    s.addTest(new JUnit4TestAdapter(GeneralCommandline.class));
    return s;
  }

}
