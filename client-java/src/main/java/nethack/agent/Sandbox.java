package nethack.agent;

import static nl.uu.cs.aplib.AplibEDSL.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import connection.SendCommandClient;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import nethack.NetHack;
import nethack.NetHack.StepType;
import nethack.object.Command;
import nethack.object.GameMode;
import nethack.object.Seed;
import nethack.utils.RenderUtils;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.utils.Pair;

public class Sandbox {
	static final Logger logger = LogManager.getLogger(Sandbox.class);
	static RenderUtils renderUtils;
	public static TacticLib tacticLib = new TacticLib();

	public static void main(String[] args) throws Exception {
		// Initialize socket connection
		SendCommandClient commander = new SendCommandClient("127.0.0.1", 5001);
		if (!commander.socketReady()) {
			logger.fatal("Unsuccesful socket connection");
			return;
		}

		// Main game loop
//		NetHack nethack = new NetHack(commander, Seed.randomSeed());
		NetHack nethack = new NetHack(commander, Seed.presets[0]);
		AgentEnv env = new AgentEnv(nethack);
		AgentState state = new AgentState();
		GoalStructure G = explore();
		
		var agent = new TestAgent("player", "player").attachState(state).attachEnvironment(env).setGoal(G);
		renderUtils = new RenderUtils(nethack.gameState, state.multiLayerNav);
		
		// Now we run the agent:
		logger.info(">> Start agent loop...");
		int k = 0;
		while (G.getStatus().inProgress() && k++ < 250) {			
			Command command = nethack.waitCommand(true); 
			if (command != null) {
				StepType stepType = nethack.step(command);
				if (stepType != StepType.Valid) {
					continue;
				}
			} else {
				agent.update();
				logger.debug("** [" + k + "] agent @" + Utils.toTile(state.worldmodel.position));
			}
			state.updateState("player");
			renderUtils.render();
			commander.writeCommand("Render", "");
		}

		logger.info(String.format("Closing nethack since the loop in agent has surpassed %d steps", k));
		nethack.close();

		// Close socket connection
		logger.info("Closing connection");
		commander.close();
	}

	private static GoalStructure explore() {
		Goal G = goal("Main [Explore floor no doors]").toSolve((Pair<AgentState, WorldModel> proposal) -> {
			return false;
//			return tacticLib.explorationExhausted(proposal.fst);
		}).withTactic(FIRSTof(
				Actions.attackMonster()
					.on_(tacticLib.inCombat_and_hpNotCritical).lift(),
				Actions.addClosedDoor()
					.on_(tacticLib.exists_closedDoor).lift(),
				Actions.exploreFloor()
					.on_(tacticLib.closed_doors_listed).lift(),
				Actions.kickDoor()
					.on_(tacticLib.near_closedDoor).lift(),
				tacticLib.explore(null),
				ABORT()
				));

		return REPEAT(G.lift());
	}

}
