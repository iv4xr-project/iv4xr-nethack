package nethack;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import util.Config;
import util.TestConfig;

/** Unit test for simple nethack.App. */
public class NetHackTest extends TestCase {
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public NetHackTest(String testName) {
    super(testName);
  }

  /**
   * @return the suite of tests being tested
   */
  public static Test suite() {
    return new TestSuite(nethack.NetHackTest.class);
  }

  /** Rigorous Test :-) */
  public void testQuaff(NetHack netHack) {
    TestConfig.setConfigFile("testConfig.properties");
    System.out.println(Config.getLogConfig());
    //    NetHack Replay.getActions("testHallucination");

    assertTrue(true);
    assertFalse(true);
  }
}
