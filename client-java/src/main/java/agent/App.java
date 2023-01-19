package agent;

import agent.navigation.NavUtils;
import connection.ConnectionLoggers;
import connection.SendCommandClient;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import nethack.NetHack;
import nethack.NetHack.StepType;
import agent.navigation.NavTactic;
import nethack.object.Command;
import nethack.object.EntityType;
import nethack.object.Player;
import nethack.object.Seed;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static nl.uu.cs.aplib.AplibEDSL.*;

public class App {
    static final Logger connectionLogger = LogManager.getLogger(ConnectionLoggers.ConnectionLogger);
    static final Logger agentLogger = LogManager.getLogger(AgentLoggers.AgentLogger);

    public static void main(String[] args) throws Exception {
        // Initialize socket connection
        SendCommandClient commander = new SendCommandClient("127.0.0.1", 5001);
        if (!commander.socketReady()) {
            connectionLogger.fatal("Unsuccessful socket connection");
            return;
        }

        // Main game loop
//		NetHack nethack = new NetHack(commander, Seed.randomSeed());
        NetHack nethack = new NetHack(commander, Seed.presets[0]);
        AgentEnv env = new AgentEnv(nethack);
        AgentState state = new AgentState();
        GoalStructure G = explore();

        TestAgent agent = new TestAgent("player", "player").attachState(state).attachEnvironment(env).setGoal(G);

        agentLogger.info(">> Start agent loop...");
        int k = 0;
        // Now we run the agent:
        while (G.getStatus().inProgress() && k++ < 400) {
            Command command = nethack.waitCommand(true);
            if (command != null) {
                StepType stepType = nethack.step(command);
                if (stepType != StepType.Valid) {
                    continue;
                }
            } else {
                agent.update();
                agentLogger.debug(String.format("** [%d] agent @%s", k, NavUtils.toTile(state.worldmodel.position)));
            }
            state.updateState(Player.id);
            state.render();
            commander.writeCommand("Render", "");
        }

        agentLogger.info(String.format("Closing nethack since the loop in agent has surpassed %d steps", k));
        nethack.close();

        // Close socket connection
        connectionLogger.info("Closing connection");
        commander.close();
    }

    private static GoalStructure explore() {
        Goal G = goal("Main [Explore floor]").toSolve((Pair<AgentState, WorldModel> proposal) -> {
            return false;
//			return tacticLib.explorationExhausted(proposal.fst);
        }).withTactic(FIRSTof(
                Actions.attackMonster()
                        .on_(Predicates.inCombat_and_hpNotCritical).lift(),
                Actions.kickDoor()
                        .on_(Predicates.near_closedDoor).lift(),
                // Navigate to closed door
                NavTactic.navigateToWorldEntity((List<WorldEntity> we) -> {
                    return EntitySelector.selectFirst(EntitySelector.closedDoor(EntitySelector.entityTypeSelector(we, EntityType.DOOR)));
                }),
                NavTactic.explore(),
                // Navigate to stairs
                NavTactic.navigateToWorldEntity((List<WorldEntity> we) -> {
                    return EntitySelector.selectFirst(EntitySelector.entityTypeSelector(we, EntityType.STAIRS_DOWN));
                }),
                ABORT()
        ));

        return REPEAT(G.lift());
    }
}
