package agent;

import agent.iv4xr.AgentEnv;
import agent.iv4xr.AgentLTL;
import agent.iv4xr.AgentState;
import agent.navigation.GridSurface;
import agent.navigation.hpastar.Cluster;
import agent.navigation.hpastar.smoother.Direction;
import agent.strategy.GoalLib;
import connection.SocketClient;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import nethack.NetHack;
import nethack.enums.CommandEnum;
import nethack.enums.Condition;
import nethack.object.Command;
import nethack.object.GameState;
import nethack.object.Player;
import nethack.object.Turn;
import nethack.object.items.Item;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class App {
//  public static void main(String[] args) throws Exception {
//    // Initialize socket connection
//    SocketClient client = new SocketClient();
////    for (int i = 0; i < 1; i++) {
////      runAgent(client);
////    }
//
//    do {
//      runAgent(client);
//    } while (Config.getAutoRestart());
//
//    client.close();
//  }

  public static void main(String[] args) throws Exception {
    SocketClient client = new SocketClient();

    Replay replay = new Replay("src/test/resources/nethack/camera.log");
    NetHack nethack = new NetHack(client, replay);

    Item cameraItem = nethack.gameState.player.inventory.items[12];
    assert "expensive camera".equals(cameraItem.entityInfo.name);

    nethack.apply(cameraItem, Direction.West);
    nethack.render();
    assert "The kitten is blinded by the flash!".equals(nethack.gameState.message);
    assert !(
            nethack.gameState.player.conditions.hasCondition(Condition.BLIND));

    nethack.step(
            List.of(
                    new Command(CommandEnum.COMMAND_APPLY),
                    new Command(cameraItem.symbol),
                    new Command('<')));
    nethack.render();
    assert "You take a picture of the ceiling.".equals(nethack.gameState.message);
    assert !(
            nethack.gameState.player.conditions.hasCondition(Condition.BLIND));
//            "Player took photograph of ceiling");

    nethack.step(
            List.of(
                    new Command(CommandEnum.COMMAND_APPLY),
                    new Command(cameraItem.symbol),
                    new Command('>')));
    nethack.render();
    assert "You take a picture of the floor.".equals(nethack.gameState.message);
    assert !(
            nethack.gameState.player.conditions.hasCondition(Condition.BLIND));
//            "Player took photograph of floor");

    nethack.step(
            List.of(
                    new Command(CommandEnum.COMMAND_APPLY),
                    new Command(cameraItem.symbol),
                    new Command('.')));
    nethack.render();
    assert "You are blinded by the flash!".equals(nethack.gameState.message);
    assert (
            nethack.gameState.player.conditions.hasCondition(Condition.BLIND));
//            "Player should be blinded when photographing self");

    Pattern pattern = Pattern.compile("\\(0:(\\d+)\\)");
    int number;

    do {
      cameraItem = nethack.gameState.player.inventory.items[12];
      Matcher matcher = pattern.matcher(cameraItem.description);
      boolean found = matcher.find();
      assert found;
      number = Integer.parseInt(matcher.group(1));

      nethack.apply(cameraItem, Direction.South);
    } while (number > 0);

    nethack.render();
    assert "Nothing happens.".equals(nethack.gameState.message);
    nethack.close();
    client.close();

    assert false;
  }

  private static void runAgent(SocketClient commander) {
    // Whether to collect coverage is specified in the config file
    boolean collectCoverage = Config.getCollectCoverage();
    if (collectCoverage) {
      commander.sendResetCoverage();
    }

    NetHack nethack;
    String fileName = Config.getReplayFile();
    if (fileName != null) {
      nethack = new NetHack(commander, new Replay(fileName));
    } else {
      nethack = new NetHack(commander, Config.getCharacter(), Config.getSeed());
    }

    AgentEnv env = new AgentEnv(nethack);
    AgentState state = new AgentState();
    GoalStructure G = GoalLib.explore();

    // Update state after init to initialize NavGraph correctly
    TestAgent agent =
        new TestAgent(Player.ID, "player").attachState(state).attachEnvironment(env).setGoal(G).addLTL(AgentLTL.scoreIncreasing(), AgentLTL.hp(), AgentLTL.energy(), AgentLTL.lvlIncreasing(), AgentLTL.hungerState(), AgentLTL.experienceIncreasing(), AgentLTL.turnIncreasing(), AgentLTL.walkable(), AgentLTL.adjacent());
    state.updateState(Player.ID);

    boolean successful = fastForwardToTurn(Config.getStartTurn(), agent, state, G);
    // Only enter if the goal structure is still in progress
    if (successful && G.getStatus().inProgress()) {
      mainAgentLoop(commander, agent, state, G, nethack);
    }

    // If the LTLs do not hold
    agent.evaluateLTLs();
//    assert !agent.evaluateLTLs() : "Not all LTLs are holding";


    Loggers.AgentLogger.info("Closing NetHack since the loop in agent has terminated");
    nethack.close();
    if (collectCoverage) {
      commander.sendSaveCoverage();
    }
  }

  private static boolean fastForwardToTurn(
      Turn desiredTurn, TestAgent agent, AgentState state, GoalStructure G) {
    GameState gameState = state.app().gameState;
    if (gameState.stats.turn.equals(desiredTurn)) {
      return true;
    }

    Loggers.AgentLogger.info("Start automatic agent loop...");
    Sounds.disableSound();
    ProgressBar bar = new ProgressBar();
    Stopwatch stopwatch = new Stopwatch(true);

    while (gameState.stats.turn.compareTo(desiredTurn) < 0 && G.getStatus().inProgress()) {
      agent.update();
      // Game terminated or probably stuck
      if (gameState.done || gameState.stats.turn.step > 20) {
        Loggers.AgentLogger.info("Game done or stuck");
        return false;
      }

      bar.updateTurn(gameState.stats.turn, desiredTurn);
    }
    stopwatch.printTotal("Running automatic loop");
    System.out.printf(
        "Steps per second: %f (%fs)%n",
        gameState.stats.turn.time / stopwatch.total(),
        stopwatch.total() / gameState.stats.turn.time);
    Sounds.setSound(Config.getSoundState());
    state.updateState(Player.ID);
    state.render();
    return true;
  }

  private static void mainAgentLoop(
      SocketClient commander, TestAgent agent, AgentState state, GoalStructure G, NetHack netHack) {
    Loggers.AgentLogger.info("Start agent loop...");
    // Now we run the agent:
    while (G.getStatus().inProgress()) {
      List<Command> commands = netHack.waitCommands(true);
      if (commands != null) {
        NetHack.StepType stepType = netHack.step(commands);
        if (stepType == NetHack.StepType.Terminated) {
          break;
        } else if (stepType != NetHack.StepType.Valid) {
          continue;
        }
      } else {
        agent.update();
        Loggers.AgentLogger.debug("agent @%s", state.worldmodel.player.current.location);
      }

      // Need to update state for render
      state.updateState(Player.ID);
      state.render();
      commander.sendRender();
    }

    state.render();
    commander.sendRender();
  }

  private static void printAbsNodes(AgentState state) {
    GridSurface surface = state.area();
    for (Cluster cluster : surface.hierarchicalMap.clusters) {
      if (cluster.entrancePoints.isEmpty()) {
        continue;
      }
      System.out.printf(
          "%s: %s%n",
          cluster.id,
          cluster.entrancePoints.stream()
              .map(
                  entrancePoint ->
                      surface.hierarchicalMap.abstractGraph.getNode(entrancePoint.abstractNodeId)
                          .info
                          .position)
              .collect(Collectors.toList()));
    }
  }
}
