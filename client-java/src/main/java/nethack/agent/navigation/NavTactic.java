package nethack.agent.navigation;

import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.IntVec2D;
import nethack.agent.AgentState;
import nethack.utils.NethackSurface_NavGraph.Tile;
import nl.uu.cs.aplib.mainConcepts.SimpleState;
import nl.uu.cs.aplib.mainConcepts.Tactic;
import nl.uu.cs.aplib.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


public class NavTactic {
    public static Tactic navigateTo(Pair<Integer, Tile> location) {
        return NavAction.navigateTo(location.fst, location.snd.pos.x, location.snd.pos.y).lift();
    }

    // Construct a tactic that would guide the agent to a tile to the target entity.
    public static Tactic navigateTo(String targetId) {
        return NavAction.navigateTo(targetId).lift();
    }

    public static Tactic navigateToWorldEntity(Function<List<WorldEntity>, WorldEntity> entitySelector) {
        return NavAction.navigateTo()
                .on((AgentState S) -> {
                    if (!S.agentIsAlive()) {
                        System.out.print("Cannot navigate since agent is dead");
                        return null;
                    }
                    WorldEntity a = S.worldmodel.elements.get(S.worldmodel().agentId);
                    WorldEntity e = entitySelector.apply(new ArrayList<>(S.worldmodel.elements.values()));
                    if (e == null) {
                        return null;
                    }
                    IntVec2D from = NavUtils.loc2(S.worldmodel.position);
                    IntVec2D to = NavUtils.loc2(e.position);
                    List<Pair<Integer, Tile>> path = NavUtils.adjustedFindPath(S, NavUtils.levelId(a), from, NavUtils.levelId(e), to);
                    if (path == null) {
                        System.out.print("No path aparently");
                        return null;
                    }
                    System.out.print("Found path to stairs down");
                    return path.get(1).snd;
                }).lift();
    }

    // Construct a tactic that would guide the agent to a tile adjacent to the location.
    public static Tactic navigateNextTo(Pair<Integer, Tile> location, boolean allowDiagonally) {
        return NavAction.navigateTo(location.fst, location.snd.pos.x, location.snd.pos.y).on_((AgentState S) -> {
            WorldEntity player = S.worldmodel.elements.get(S.worldmodel.agentId);
            Tile p = NavUtils.toTile(player.position);
            return !NavUtils.adjacent(p, location.snd, allowDiagonally);
        }).lift();
    }

    // Construct a tactic that would guide the agent to a tile adjacent to the target entity.
    public static Tactic navigateNextTo(String targetId, boolean allowDiagonally) {
        return NavAction.navigateNextTo(targetId, allowDiagonally).lift();
        //return NavAction.navigateTo(targetId).on_((AgentState S) -> !S.nextToEntity(targetId, allowDiagonally)).lift();
    }

    public static boolean exploringDone(SimpleState S) {
        return !NavAction.explore(null).isEnabled(S);
    }

    // Explore version that tries to further explore the level
    public static Tactic explore() {
        return NavTactic.explore(null);
    }

    // Explores to a given location, if location is null then it explores new parts of the level
    public static Tactic explore(Pair<Integer, Tile> heuristicLocation) {
        return NavAction.explore(heuristicLocation).lift();
    }
}
