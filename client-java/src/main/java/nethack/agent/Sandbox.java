package nethack.agent;

import static nl.uu.cs.aplib.AplibEDSL.*;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import connection.SendCommandClient;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import nethack.NetHack;
import nethack.NetHack.StepType;
import nethack.agent.navigation.NavAction;
import nethack.agent.navigation.NavTactic;
import nethack.agent.navigation.NavUtils;
import nethack.object.Command;
import nethack.object.EntityType;
import nethack.object.Seed;
import nethack.utils.RenderUtils;
import nethack.utils.NethackSurface_NavGraph.Tile;
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
				logger.debug("** [" + k + "] agent @" + NavUtils.toTile(state.worldmodel.position));
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
					.on_(Predicates.inCombat_and_hpNotCritical).lift(),
				Actions.kickDoor()
					.on_(Predicates.near_closedDoor).lift(),
				NavAction.navigateToSomething()
					.on((AgentState S) -> {
						// return three possible values:
						// (1) null --> the action is not enabled
						// (2 disabled) empty array of tiles --> the agent is already next to the target
						// (3) a singleton array of tile --> the next tile to move to
						//
						if (!S.agentIsAlive()) {
							System.out.print("Cannot navigate since agent is dead");
							return null;
						}
						var a = S.worldmodel.elements.get(S.worldmodel().agentId);
						Tile agentPos = NavUtils.toTile(S.worldmodel.position);
						
						List<WorldEntity> doors = S.worldmodel.elements
								.values().stream().filter(e -> e.type == EntityType.DOOR.name()).collect(Collectors.toList());

						doors = doors.stream().filter(d -> (boolean)d.properties.get("closed")).collect(Collectors.toList());
						if (doors.size() == 0) {
							return null;
						}
						
						WorldEntity e = doors.get(0);
						Tile target = NavUtils.toTile(e.position);
//						if (NavUtils.levelId(a) == NavUtils.levelId(e) && NavUtils.adjacent(agentPos, target, false)) {
//							Tile[] nextTile = {};
//							System.out.print("Found path");
//							return nextTile;
//						}
						var path = NavUtils.adjustedFindPath(S, NavUtils.levelId(a), agentPos.x, agentPos.y, NavUtils.levelId(e), target.x,
								target.y);
						if (path == null) {
							System.out.print("No path aparently");
							return null;
						}
						System.out.print("Found path");
						return path.get(1).snd;
					}).lift(),
				NavTactic.explore(),
//				Actions.addClosedDoor()
//					.on_(Predicates.closed_door_set.negate().and(Predicates.closed_door_exists)).lift(),
//				SEQ(Actions.openClosedDoor()
//					.on_(Predicates.closed_door_set).lift(), ABORT()),
				ABORT()
				));

		return REPEAT(G.lift());
	}

}
