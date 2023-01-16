package nethack.agent;

import eu.iv4xr.framework.mainConcepts.WorldEntity;
import nethack.agent.navigation.NavUtils;
import nethack.utils.NethackSurface_NavGraph.Tile;
import nl.uu.cs.aplib.mainConcepts.Tactic;

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
    /**
     * This constructs a "default" tactic to interact with an entity. The tactic is
     * enabled if the entity is known in the agent's state/wom, and if it is
     * adjacent to the agent.
     */
    public Tactic interactTac(String targetId) {
        return Actions.interact(targetId).on((AgentState S) -> {
            if (!S.agentIsAlive())
                return null;
            WorldEntity a = S.worldmodel.elements.get(S.worldmodel().agentId);
            Tile agentPos = NavUtils.toTile(S.worldmodel.position);
            WorldEntity e = S.worldmodel.elements.get(targetId);
            if (e == null || NavUtils.levelId(a) != NavUtils.levelId(e)) {
                return null;
            }
            Tile target = NavUtils.toTile(e.position);
            if (NavUtils.adjacent(agentPos, target, true)) {
                return target;
            }
            return null;
        }).lift();
    }
}
