package nethack.agent;

import static nl.uu.cs.aplib.AplibEDSL.SUCCESS;
import static nl.uu.cs.aplib.AplibEDSL.FIRSTof;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import connection.SendCommandClient;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import nethack.NetHack;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;

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
		GoalStructure G = FIRSTof(SUCCESS(), SUCCESS());
		var agent = new TestAgent("player","player")
				.attachState(state)
				.attachEnvironment(env)
				.setGoal(G);
		
//		nethack.loop();
//		nethack.close();
//
//		// Close socket connection
//		logger.info("Closing connection");
//		commander.close();

		// Now we run the agent:
		System.out.println(">> Start agent loop...") ;
		int k = 0 ;
		while(G.getStatus().inProgress() && k++ < 150) {
			agent.update();
			System.out.println("** [" + k + "] agent @" + Utils.toTile(state.worldmodel.position)) ;
			// delay to slow it a bit for displaying:
			Thread.sleep(50);
		}	
		//System.exit(0);	
		
		nethack.close();

		// Close socket connection
		logger.info("Closing connection");
		commander.close();
	}

}
