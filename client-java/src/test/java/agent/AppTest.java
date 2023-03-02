package agent;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import util.Config;
import util.TestConfig;

/** Unit test for simple nethack.App. */
public class AppTest extends TestCase {
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public AppTest(String testName) {
    super(testName);
  }

  /**
   * @return the suite of tests being tested
   */
  public static Test suite() {
    return new TestSuite(AppTest.class);
  }

  /** Rigorous Test :-) */
  public void testApp() {
    TestConfig.setConfigFile("testConfig.properties");
    System.out.println(Config.getLogConfig());
    assertTrue(true);
    assertFalse(true);
  }
}
