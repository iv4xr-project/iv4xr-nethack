package nethack.agent;

import static nl.uu.cs.aplib.AplibEDSL.SUCCESS;
import static nl.uu.cs.aplib.AplibEDSL.goal;
import static nl.uu.cs.aplib.AplibEDSL.ABORT;
import static nl.uu.cs.aplib.AplibEDSL.FIRSTof;
import static nl.uu.cs.aplib.AplibEDSL.REPEAT;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import connection.SendCommandClient;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import nethack.NetHack;
import nethack.object.Command;
import nethack.utils.RenderUtils;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.utils.Pair;

/**
 * A simple demo of controlling MiniDungeon using Goals. We set a simple goal
 * for the agent to find and pick a specific scroll, and then to find the shrine
 * to use the scroll on it. The used goals includes a tactic when the agent is
 * attacked by monsters while it is on its way.
 * 
 * @author wish
 */
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
		NetHack nethack = new NetHack(commander);
		AgentEnv env = new AgentEnv(nethack);
		AgentState state = new AgentState();
		GoalStructure G = explore();
		
		var agent = new TestAgent("player", "player").attachState(state).attachEnvironment(env).setGoal(G);
		renderUtils = new RenderUtils(nethack.gameState, state.multiLayerNav);
		
		
		// Now we run the agent:
		logger.info(">> Start agent loop...");
		int k = 0;
		while (G.getStatus().inProgress() && k++ < 150) {
			Command command = nethack.waitCommand(true); 
			if (command != null) {
				if (command == Command.COMMAND_EXTLIST) {
					Command.prettyPrintActions();
					continue;
				}
				
				nethack.step(command);
				state.updateState("player");
			} else {
				agent.update();
				logger.debug("** [" + k + "] agent @" + Utils.toTile(state.worldmodel.position));
			}
			renderUtils.render();
		}

		nethack.close();

		// Close socket connection
		logger.info("Closing connection");
		commander.close();
	}

	private static GoalStructure explore() {
		Goal G = goal("Explore").toSolve((Pair<AgentState, WorldModel> proposal) -> {
			var S = proposal.fst;
			WorldModel previouswom = S.worldmodel;
			WorldModel newObs = proposal.snd;
			WorldEntity e = previouswom.getElement("Blah");
			if (e == null) {
				return false;
			}
			var a = S.worldmodel.elements.get(S.worldmodel().agentId);
			var solved = Utils.levelId(a) == Utils.levelId(e)
					&& Utils.adjacent(Utils.toTile(newObs.position), Utils.toTile(e.position), true);
			// System.out.println(">>> checking goal") ;
			return solved;
		}).withTactic(FIRSTof(
				tacticLib.attackMonsterAction()
					.on_(tacticLib.inCombat_and_hpNotCritical).lift(),
				tacticLib.kickDoorAction()
					.on_(tacticLib.near_closedDoor).lift(),
				tacticLib.explore(null), ABORT()));

		return REPEAT(G.lift());
	}

}
