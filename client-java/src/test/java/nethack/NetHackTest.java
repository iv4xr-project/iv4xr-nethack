package nethack;

import static org.junit.jupiter.api.Assertions.*;

import agent.navigation.hpastar.smoother.Direction;
import com.fasterxml.jackson.databind.ObjectMapper;
import connection.SocketClient;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import nethack.enums.CommandEnum;
import nethack.enums.Condition;
import nethack.object.Command;
import nethack.object.items.Item;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.MutationData;
import util.Replay;
import util.TestConfig;

/** Unit test for simple nethack.App. */
public class NetHackTest {
  SocketClient client;

  @BeforeEach
  public void setup() {
    TestConfig.setConfigFile("testConfig.properties");
    client = new SocketClient();
  }

  @AfterEach
  public void teardown() throws IOException {
    client.close();

    // Init new connection to perform exit
    // The reason the original client cannot do that since we don't know whether the handler has
    // stopped
    client = new SocketClient();
    client.sendExitServer();
    client = null;
  }

  /** Test for hallucination potion */
  @Test
  public void testQuaff() {
    Replay replay = new Replay("src/test/resources/nethack/quaffHallucinationPotion.log");
    NetHack nethack = new NetHack(client, replay);
    assertFalse(
        nethack.previousGameState.player.conditions.hasCondition(Condition.HALLUCINATING),
        "Player is not hallucinating before drinking the potion");
    assertTrue(
        nethack.gameState.player.conditions.hasCondition(Condition.HALLUCINATING),
        "Player is hallucinating after drinking the potion");
  }

  //  @ParameterizedTest
  //  @MethodSource("cameraMutants")
  //  public void testCamera(MutationData.Mutant mutant) {
  @Test
  public void testCamera() {
    Replay replay = new Replay("src/test/resources/nethack/camera.log");
    NetHack nethack = new NetHack(client, replay);

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
            new Command('>')));
    nethack.render();
    assertEquals("You take a picture of the floor.", nethack.gameState.message);
    assertFalse(
        nethack.gameState.player.conditions.hasCondition(Condition.BLIND),
        "Player took photograph of floor");

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

    nethack.close();
  }

  static Stream<MutationData.Mutant> cameraMutants() {
    // Create ObjectMapper instance
    ObjectMapper objectMapper = new ObjectMapper();

    // Read JSON file and parse it into Data object
    MutationData data = null;
    try {
      data =
          objectMapper.readValue(
              new File("src/test/resources/mutation_info.json"), MutationData.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return data.mutants.stream();
  }

  @Test
  public void testChoke() {}

  public void testChokeEat() {}

  public void testChokeAmulet() {
    Replay replay = new Replay("src/test/resources/nethack/amuletOfStrangulation.log");
    NetHack nethack = new NetHack(new SocketClient(), replay.character, replay.seed);
  }
}
