package agent.strategy;

import static nl.uu.cs.aplib.AplibEDSL.ABORT;

import agent.AgentLoggers;
import agent.iv4xr.AgentState;
import agent.navigation.strategy.NavUtils;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import nl.uu.cs.aplib.mainConcepts.Tactic;
import org.apache.logging.log4j.Logger;

/**
 * Provide several basic actions and tactics.
 *
 * <p>Keep in mind that the provided navigation and exploration tactics/goals currently has no
 * ability to deal with items that block a corridor. The solution is for now to just generate
 * another dungeon where we have no corridors are not blocked by items (use another random seed, for
 * example). A better fix would be to have a smarter navigation and exploration. TO DO.
 *
 * @author wish
 */
public class TacticLib {
  static final Logger logger = AgentLoggers.AgentLogger;

  public static Tactic abortOnDeath() {
    return ABORT()
        .on(
            (AgentState S) -> {
              boolean agentAlive = S.agentIsAlive();
              if (agentAlive) {
                return null;
              }
              logger.info(">>> Agent dead, abort");
              return true;
            });
  }

  /**
   * This constructs a "default" tactic to interact with an entity. The tactic is enabled if the
   * entity is known in the agent's state/wom, and if it is adjacent to the agent.
   */
  public Tactic interactTac(String targetId) {
    return Actions.interact(targetId)
        .on(
            (AgentState S) -> {
              WorldEntity e = S.worldmodel.elements.get(targetId);
              if (e == null
                  || NavUtils.levelNr(S.worldmodel.position) != NavUtils.levelNr(e.position)) {
                return null;
              }
              if (NavUtils.adjacent(
                  NavUtils.loc2(S.worldmodel.position), NavUtils.loc2(e.position), true)) {
                return NavUtils.loc2(e.position);
              }
              return null;
            })
        .lift();
  }
}
