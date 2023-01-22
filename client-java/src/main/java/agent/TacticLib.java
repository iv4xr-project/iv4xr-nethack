package agent;

import agent.navigation.NavUtils;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import agent.navigation.surface.Tile;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.mainConcepts.Tactic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static nl.uu.cs.aplib.AplibEDSL.*;

/**
 * Provide several basic actions and tactics.
 *
 * <p>
 * Keep in mind that the provided navigation and exploration tactics/goals
 * currently has no ability to deal with items that block a corridor. The
 * solution is for now to just generate another dungeon where we have no
 * corridors are not blocked by items (use another random seed, for example). A
 * better fix would be to have a smarter navigation and exploration. TO DO.
 *
 * @author wish
 */
public class TacticLib {
    static final Logger logger = LogManager.getLogger(AgentLoggers.AgentLogger);
    /**
     * This constructs a "default" tactic to interact with an entity. The tactic is
     * enabled if the entity is known in the agent's state/wom, and if it is
     * adjacent to the agent.
     */
    public Tactic interactTac(String targetId) {
        return Actions.interact(targetId).on((AgentState S) -> {
            WorldEntity a = S.worldmodel.elements.get(S.worldmodel().agentId);
            Tile agentPos = NavUtils.toTile(S.worldmodel.position);
            WorldEntity e = S.worldmodel.elements.get(targetId);
            if (e == null || NavUtils.levelNr(a) != NavUtils.levelNr(e)) {
                return null;
            }
            Tile target = NavUtils.toTile(e.position);
            if (NavUtils.adjacent(agentPos, target, true)) {
                return target;
            }
            return null;
        }).lift();
    }

    public static Tactic abortOnDeath() {
        return ABORT().on((AgentState S) -> {
            boolean agentAlive = S.agentIsAlive();
            if (agentAlive) {
                return null;
            }
            logger.info(">>> Agent dead, abort");
            return true;
        });
    }
}
