package net.gwerder.java.mailvortex.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  MailVortexTest.class
})

public class MainSuite {

  public static junit.framework.Test suite() {    return new junit.framework.JUnit4TestAdapter(MailVortexTest.class);}
  // the class remains empty,
  // used only as a holder for the above annotations
}