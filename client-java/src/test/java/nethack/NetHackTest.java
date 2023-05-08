package nethack;

import connection.SocketClient;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import nethack.enums.Condition;
import util.Replay;
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

  /** Test for hallucination potion */
  public void testQuaff() {
    TestConfig.setConfigFile("testConfig.properties");
    Replay replay = new Replay("src/test/resources/nethack/quaffHallucinationPotion.log");
    NetHack nethack = new NetHack(new SocketClient(), replay.character, replay.seed);
    nethack.replay(replay);
    assertFalse(
        "Player is not hallucinating before drinking the potion",
        nethack.previousGameState.player.conditions.hasCondition(Condition.HALLUCINATING));
    assertTrue(
        "Player is hallucinating after drinking the potion",
        nethack.gameState.player.conditions.hasCondition(Condition.HALLUCINATING));
  }
}
