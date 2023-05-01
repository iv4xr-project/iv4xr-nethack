package agent;

import agent.iv4xr.AgentEnv;
import agent.iv4xr.AgentState;
import agent.navigation.GridSurface;
import agent.navigation.hpastar.Cluster;
import agent.strategy.GoalLib;
import connection.SocketClient;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import java.util.List;
import java.util.stream.Collectors;
import nethack.NetHack;
import nethack.object.Command;
import nethack.object.GameState;
import nethack.object.Player;
import nethack.object.Turn;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import util.*;

public class App {
  public static void main(String[] args) throws Exception {
    // Initialize socket connection
    SocketClient client = new SocketClient();
    runAgent(client);
    client.close();
  }

  private static void runAgent(SocketClient commander) {
    // Whether to collect coverage is specified in the config file
    boolean collectCoverage = Config.getCollectCoverage();
    if (collectCoverage) {
      commander.sendResetCoverage();
    }
    NetHack nethack = new NetHack(commander, Config.getCharacter(), Config.getSeed());
    AgentEnv env = new AgentEnv(nethack);
    AgentState state = new AgentState();
    GoalStructure G = GoalLib.explore();

    // Update state after init to initialize NavGraph correctly
    TestAgent agent =
        new TestAgent(Player.ID, "player").attachState(state).attachEnvironment(env).setGoal(G);
    state.updateState(Player.ID);

    fastForwardToTurn(Config.getStartTurn(), agent, state, G);
    // Only enter if the goal structure is still in progress
    if (G.getStatus().inProgress()) {
      mainAgentLoop(commander, agent, state, G, nethack);
    }

    Loggers.AgentLogger.info("Closing NetHack since the loop in agent has terminated");
    nethack.close();
    if (collectCoverage) {
      commander.sendSaveCoverage();
    }
  }

  private static void fastForwardToTurn(
      Turn desiredTurn, TestAgent agent, AgentState state, GoalStructure G) {
    GameState gameState = state.app().gameState;
    if (gameState.stats.turn.equals(desiredTurn)) {
      return;
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
        break;
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
