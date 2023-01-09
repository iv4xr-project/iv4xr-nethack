package nethack.agent;

import static nl.uu.cs.aplib.AplibEDSL.SUCCESS;
import static nl.uu.cs.aplib.AplibEDSL.goal;
import static nl.uu.cs.aplib.AplibEDSL.ABORT;
import static nl.uu.cs.aplib.AplibEDSL.FIRSTof;
import static nl.uu.cs.aplib.AplibEDSL.REPEAT;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import connection.SendCommandClient;
import eu.iv4xr.framework.extensions.pathfinding.Sparse2DTiledSurface_NavGraph.Tile;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import nethack.NetHack;
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
		nethack.init();

		AgentEnv env = new AgentEnv(nethack);
		AgentState state = new AgentState();

		GoalStructure G = explore();
		var agent = new TestAgent("player", "player").attachState(state).attachEnvironment(env).setGoal(G);

		// Now we run the agent:
		System.out.println(">> Start agent loop...");
		int k = 0;
		while (G.getStatus().inProgress() && k++ < 150) {
			System.in.read();
			agent.update();
			System.out.println("** [" + k + "] agent @" + Utils.toTile(state.worldmodel.position));
			// delay to slow it a bit for displaying:
			nethack.render();
//			System.out.println(state.multiLayerNav.toString());
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
					&& Utils.adjacent(Utils.toTile(newObs.position), Utils.toTile(e.position));
			// System.out.println(">>> checking goal") ;
			return solved;
		}).withTactic(FIRSTof(tacticLib.attackMonsterAction().on_(tacticLib.inCombat_and_hpNotCritical).lift(),
				tacticLib.explore(null), ABORT()));

//				Later have monster attacks
//				tacticLib.attackMonsterAction().lift(), 

//				   FIRSTof(tacticLib.explore(null),
//						   //Abort().on_(S -> { System.out.println("### about to abort") ; return false;}).lift(), 
//				   		   ABORT()) 
//				  )
//				;

		return REPEAT(G.lift());
	}

}
