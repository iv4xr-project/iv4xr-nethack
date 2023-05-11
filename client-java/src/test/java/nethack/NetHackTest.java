package nethack;

import static org.junit.jupiter.api.Assertions.*;

import agent.navigation.hpastar.smoother.Direction;
import connection.SocketClient;
import java.util.List;
import nethack.enums.CommandEnum;
import nethack.enums.Condition;
import nethack.object.Command;
import nethack.object.items.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Replay;
import util.TestConfig;

/** Unit test for simple nethack.App. */
public class NetHackTest {
  @BeforeEach
  public void setup() {
    TestConfig.setConfigFile("testConfig.properties");
  }

  /** Test for hallucination potion */
  @Test
  public void testQuaff() {
    Replay replay = new Replay("src/test/resources/nethack/quaffHallucinationPotion.log");
    NetHack nethack = new NetHack(new SocketClient(), replay.character, replay.seed);
    nethack.replay(replay);
    assertFalse(
        nethack.previousGameState.player.conditions.hasCondition(Condition.HALLUCINATING),
        "Player is not hallucinating before drinking the potion");
    assertTrue(
        nethack.gameState.player.conditions.hasCondition(Condition.HALLUCINATING),
        "Player is hallucinating after drinking the potion");
  }

  @Test
  public void testCamera() {
    Replay replay = new Replay("src/test/resources/nethack/camera.log");
    NetHack nethack = new NetHack(new SocketClient(), replay.character, replay.seed);
    Item cameraItem = nethack.gameState.player.inventory.items[12];
    assertEquals("expensive camera", cameraItem.entityInfo.name);

    nethack.apply(cameraItem, Direction.West);
    nethack.render();
    assertEquals("The kitten is blinded by the flash!", nethack.gameState.message);
    assertFalse(
        nethack.gameState.player.conditions.hasCondition(Condition.BLIND),
        "Player took photograph of cat");

    nethack.step(
        List.of(
            new Command(CommandEnum.COMMAND_APPLY),
            new Command(cameraItem.symbol),
            new Command('<')));
    nethack.render();
    assertEquals("You take a picture of the ceiling.", nethack.gameState.message);
    assertFalse(
        nethack.gameState.player.conditions.hasCondition(Condition.BLIND),
        "Player took photograph of ceiling");

    nethack.step(
        List.of(
            new Command(CommandEnum.COMMAND_APPLY),
            new Command(cameraItem.symbol),
            new Command('.')));
    nethack.render();
    assertEquals("You are blinded by the flash!", nethack.gameState.message);
    assertTrue(
        nethack.gameState.player.conditions.hasCondition(Condition.BLIND),
        "Player should be blinded when photographing self");
  }

  //  public void testChoke() {
  //
  //  }
}
