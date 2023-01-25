package agent;

import static nl.uu.cs.aplib.AplibEDSL.*;

import agent.navigation.NavTactic;
import agent.selector.EntitySelector;
import agent.selector.TileSelector;
import connection.ConnectionLoggers;
import connection.SocketClient;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import nethack.NetHack;
import nethack.NetHack.StepType;
import nethack.enums.Command;
import nethack.object.Player;
import nethack.object.Seed;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {
  static final Logger connectionLogger = LogManager.getLogger(ConnectionLoggers.ConnectionLogger);
  static final Logger agentLogger = LogManager.getLogger(AgentLoggers.AgentLogger);

  public static void main(String[] args) throws Exception {
    // Initialize socket connection
    SocketClient client = new SocketClient("127.0.0.1", 5001);
    if (!client.socketReady()) {
      connectionLogger.fatal("Unsuccessful socket connection");
      return;
    }

    runAgent(client);

    // Close socket connection
    connectionLogger.info("Closing connection");
    client.close();
  }

  private static void runAgent(SocketClient commander) {
    //    NetHack nethack = new NetHack(commander, Seed.randomSeed());
    NetHack nethack = new NetHack(commander, Seed.presets[0]);
    AgentEnv env = new AgentEnv(nethack);
    AgentState state = new AgentState();
    GoalStructure G = explore();

    // Update state after init to initialize navgraph correctly
    TestAgent agent =
        new TestAgent(Player.ID, "player").attachState(state).attachEnvironment(env).setGoal(G);
    state.updateState(Player.ID);

    agentLogger.info("Start agent loop...");
    int jumpToTurn = 1108;
    int k = 0;
    //    int jumpToTurn = 0;
    // Now we run the agent:
    agentLogger.fatal("Start now");
    while (G.getStatus().inProgress()) {
      if (state.app().gameState.stats.time < jumpToTurn) {
        agent.update();
        k++;
        continue;
      } else {
        agentLogger.fatal(String.format("Done after %d steps", k));
        System.exit(0);
      }

      Command command = nethack.waitCommand(true);
      if (command != null) {
        StepType stepType = nethack.step(command);
        if (stepType != StepType.Valid) {
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

    agentLogger.info("Closing NetHack since the loop in agent has terminated");
    nethack.close();
  }

  private static GoalStructure explore() {
    Goal G =
        goal("Main")
            .toSolve(
                (Pair<AgentState, WorldModel> proposal) -> {
                  return false;
                  //          return tacticLib.explorationExhausted(proposal.fst);
                })
            .withTactic(
                FIRSTof(
                    TacticLib.abortOnDeath(),
                    // Survival
                    Actions.attackMonster().on_(Predicates.inCombat_and_hpNotCritical).lift(),
                    Actions.eatFood().lift(),
                    NavTactic.navigateToWorldEntity(EntitySelector.money),

                    // Navigation
                    Actions.kickDoor().on(Predicates.get_lockedDoor()).lift(),
                    Actions.openDoor().on(Predicates.get_closedDoor()).lift(),
                    NavTactic.navigateNextToTile(TileSelector.closedDoorSelector, false),
                    NavTactic.explore(),

                    // Go to next level
                    NavTactic.navigateToTile(TileSelector.stairDown),
                    Actions.singleAction(Command.MISC_DOWN).on_(Predicates.on_stairs_down).lift(),

                    // Explore walls for hidden doors
                    NavTactic.navigateNextToTile(TileSelector.wallSelector, true),
                    Actions.searchWalls().lift(),
                    ABORT()));

    return G.lift(); // REPEAT(G.lift());
  }
}
