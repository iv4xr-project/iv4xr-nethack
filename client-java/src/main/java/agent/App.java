package agent;

import agent.iv4xr.AgentEnv;
import agent.iv4xr.AgentState;
import agent.strategy.GoalLib;
import agent.util.ProgressBar;
import agent.util.Sounds;
import connection.ConnectionLoggers;
import connection.SocketClient;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import nethack.Config;
import nethack.NetHack;
import nethack.enums.Command;
import nethack.object.Player;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {
  static final Logger connectionLogger = LogManager.getLogger(ConnectionLoggers.ConnectionLogger);
  static final Logger agentLogger = LogManager.getLogger(AgentLoggers.AgentLogger);

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

    fastForwardToTurn(Config.getStartTurn(), agent, state);
    mainAgentLoop(commander, agent, state, G, nethack);

    agentLogger.info("Closing NetHack since the loop in agent has terminated");
    nethack.close();
  }

  private static void fastForwardToTurn(int desiredTurnNr, TestAgent agent, AgentState state) {
    assert desiredTurnNr > 0 : "Cannot fast forward to a 0 or negative turn";

    if (desiredTurnNr == 1) {
      return;
    }

    agentLogger.info("Start automatic agent loop...");
    Sounds.disableSound();
    ProgressBar bar = new ProgressBar();
    while (state.app().gameState.stats.time < desiredTurnNr) {
      bar.update(state.app().gameState.stats.time, desiredTurnNr);
      agent.update();
    }
    Sounds.setSound(Config.getSoundState());
    state.updateState(Player.ID);
    state.render();
  }

  private static void mainAgentLoop(
      SocketClient commander, TestAgent agent, AgentState state, GoalStructure G, NetHack netHack) {
    agentLogger.info("Start agent loop...");
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
        agentLogger.debug(String.format("agent @%s", state.worldmodel.position));
      }

      // Need to update state for render
      state.updateState(Player.ID);
      state.render();
      commander.sendRender();
    }

    state.render();
    commander.sendRender();
  }
}
