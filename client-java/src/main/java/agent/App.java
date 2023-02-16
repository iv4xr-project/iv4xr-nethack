package agent;

import agent.iv4xr.AgentEnv;
import agent.iv4xr.AgentState;
import agent.navigation.NetHackSurface;
import agent.navigation.hpastar.Cluster;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.Tile;
import agent.strategy.GoalLib;
import connection.SocketClient;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.spatial.Vec3;
import java.util.stream.Collectors;
import nethack.NetHack;
import nethack.enums.Command;
import nethack.object.Player;
import nethack.object.Turn;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.utils.Pair;
import util.*;

public class App {
  public static void main(String[] args) throws Exception {
    // Initialize socket connection
    SocketClient client = new SocketClient();
    runAgent(client);
    client.close();
  }

  private static void runAgent(SocketClient commander) {
    NetHack nethack = new NetHack(commander, Config.getSeed());
    AgentEnv env = new AgentEnv(nethack);
    AgentState state = new AgentState();
    GoalStructure G = GoalLib.explore();

    // Update state after init to initialize NavGraph correctly
    TestAgent agent =
        new TestAgent(Player.ID, "player").attachState(state).attachEnvironment(env).setGoal(G);
    state.updateState(Player.ID);

    if (true) {
      fastForwardToTurn(Config.getStartTurn(), agent, state);
      mainAgentLoop(commander, agent, state, G, nethack);
    } else {
      Pair<Integer, Tile> from = NavUtils.loc3(new Vec3(6, 3, 0));
      Pair<Integer, Tile> to = NavUtils.loc3(new Vec3(6, 7, 0));
      System.out.println(state.area().hierarchicalMap);
      var path = state.hierarchicalNav.findPath(from, to);
      System.out.println(path);
    }

    Loggers.AgentLogger.info("Closing NetHack since the loop in agent has terminated");
    nethack.close();
  }

  private static void fastForwardToTurn(Turn desiredTurn, TestAgent agent, AgentState state) {
    if (state.app().gameState.stats.turn.equals(desiredTurn)) {
      return;
    }

    Loggers.AgentLogger.info("Start automatic agent loop...");
    Sounds.disableSound();
    ProgressBar bar = new ProgressBar();
    Stopwatch stopwatch = new Stopwatch(true);
    while (state.app().gameState.stats.turn.compareTo(desiredTurn) < 0) {
      bar.update(state.app().gameState.stats.turn.time, desiredTurn.time);
      agent.update();
    }
    stopwatch.printTotal("Running automatic loop");
    Sounds.setSound(Config.getSoundState());
    state.updateState(Player.ID);
    state.render();
  }

  private static void mainAgentLoop(
      SocketClient commander, TestAgent agent, AgentState state, GoalStructure G, NetHack netHack) {
    Loggers.AgentLogger.info("Start agent loop...");
    // Now we run the agent:
    while (G.getStatus().inProgress()) {
      Command command = netHack.waitCommand(true);
      if (command != null) {
        NetHack.StepType stepType = netHack.step(command);
        if (stepType != NetHack.StepType.Valid) {
          continue;
        }
      } else {
        agent.update();
        Loggers.AgentLogger.debug("agent @%s", state.worldmodel.position);
      }

      // Need to update state for render
      state.updateState(Player.ID);
      //      printAbsNodes(state);
      state.render();
      commander.sendRender();
    }

    state.render();
    commander.sendRender();
  }

  private static void printAbsNodes(AgentState state) {
    NetHackSurface surface = state.hierarchicalNav.areas.get(0);
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
    //
    //    List<Id<AbstractNode>> nodesIds =
    //        new ArrayList<>(surface.hierarchicalMap.abstractGraph.nodes.keySet());
    //    for (Id<AbstractNode> nodeId : nodesIds) {
    //      AbstractNode node = surface.hierarchicalMap.abstractGraph.getNode(nodeId);
    //      for (Id<AbstractNode> neighbourId : node.edges.keySet()) {
    //        AbstractNode neighbour = surface.hierarchicalMap.abstractGraph.getNode(neighbourId);
    //        assert neighbour.edges.containsKey(nodeId) : "Not bidirectional abs edge";
    //      }
    //    }
    //    System.out.printf("ABS NODES (%d) [", nodesIds.size());
    //    for (AbstractNode node : surface.hierarchicalMap.abstractGraph.nodes.values()) {
    //      System.out.printf(" {%s: %s nrEdges:%s}", node.nodeId, node.info.position,
    // node.edges.size());
    //    }
    //    System.out.println("]");
  }
}
