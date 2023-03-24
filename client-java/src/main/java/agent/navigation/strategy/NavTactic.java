package agent.navigation.strategy;
;
import agent.iv4xr.AgentState;
import agent.navigation.surface.Tile;
import agent.selector.EntitySelector;
import agent.selector.TileSelector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.Vec3;
import java.util.ArrayList;
import java.util.List;
import nethack.world.Level;
import nethack.world.Surface;
import nl.uu.cs.aplib.mainConcepts.SimpleState;
import nl.uu.cs.aplib.mainConcepts.Tactic;
import util.CustomVec2D;
import util.CustomVec3D;
import util.Loggers;

public class NavTactic {
  public static Tactic navigateTo(CustomVec3D location) {
    return NavAction.navigateTo(location).lift();
  }

  // Construct a tactic that would guide the agent to a tile to the target entity.
  public static Tactic navigateTo(String targetId) {
    return NavAction.navigateTo(targetId).lift();
  }

  public static Tactic navigateToWorldEntity(EntitySelector entitySelector) {
    return NavAction.navigateTo()
        .on(
            (AgentState S) -> {
              WorldEntity e =
                  entitySelector.apply(new ArrayList<>(S.worldmodel.elements.values()), S);
              if (e == null) {
                return null;
              }
              CustomVec3D nextTile =
                  NavUtils.nextLoc(
                      NavUtils.adjustedFindPath(S, S.loc(), new CustomVec3D(e.position)));
              if (nextTile == null) {
                return null;
              }
              Loggers.NavLogger.debug(
                  String.format("navigateToWorldEntity (%s) via %s", entitySelector, nextTile));
              return nextTile;
            })
        .lift();
  }

  public static Tactic navigateToTile(TileSelector tileSelector) {
    return NavAction.navigateTo()
        .on(
            (AgentState S) -> {
              Tile tile = tileSelector.apply(S);
              if (tile == null) {
                return null;
              }

              Vec3 agentPos = S.worldmodel.position;
              CustomVec3D nextLoc =
                  NavUtils.nextLoc(NavUtils.adjustedFindPath(S, S.loc(), tile.loc));
              if (nextLoc == null) {
                return null;
              }
              Loggers.NavLogger.debug("navigateToTile (%s) via %s", tileSelector, nextLoc);
              return nextLoc;
            })
        .lift();
  }

  public static Tactic navigateNextToTile(TileSelector tileSelector, boolean allowDiagonal) {
    return NavAction.navigateTo()
        .on(
            (AgentState S) -> {
              Tile tile = tileSelector.apply(S);
              if (tile == null) {
                return null;
              }

              CustomVec3D agentLoc = S.loc();
              Surface surface = S.area();
              List<CustomVec3D> path = null;
              for (CustomVec2D pos :
                  NavUtils.neighbourCoordinates(tile.pos, Level.SIZE, allowDiagonal)) {
                if (pos.equals(agentLoc.pos)) {
                  return null;
                }
                if (surface.nullTile(pos) || !surface.isWalkable(pos)) {
                  continue;
                }

                List<CustomVec3D> pathToNeighbour =
                    NavUtils.adjustedFindPath(
                        S, agentLoc, new CustomVec3D(agentLoc.lvl, agentLoc.pos));
                if (pathToNeighbour == null) {
                  continue;
                }
                if (path == null || pathToNeighbour.size() < path.size()) {
                  path = pathToNeighbour;
                }
              }

              CustomVec3D nextLoc = NavUtils.nextLoc(path);
              if (nextLoc == null) {
                return null;
              }
              Loggers.NavLogger.debug(
                  "navigateNextToTile (%s) via %s (allowDiagonal=%b)",
                  tileSelector, nextLoc, allowDiagonal);
              return nextLoc;
            })
        .lift();
  }

  // Construct a tactic that would guide the agent to a tile adjacent to the location.
  public static Tactic navigateNextTo(CustomVec3D location, boolean allowDiagonally) {
    return NavAction.navigateTo(location)
        .on_(
            (AgentState S) -> {
              return !CustomVec3D.adjacent(S.loc(), location, allowDiagonally);
            })
        .lift();
  }

  // Construct a tactic that would guide the agent to a tile adjacent to the target entity.
  public static Tactic navigateNextTo(String targetId, boolean allowDiagonally) {
    return NavAction.navigateNextTo(targetId, allowDiagonally).lift();
    // return NavAction.navigateTo(targetId).on_((AgentState S) -> !S.nextToEntity(targetId,
    // allowDiagonally)).lift();
  }

  public static boolean exploringDone(SimpleState S) {
    return !NavAction.explore(null).isEnabled(S);
  }

  // Explore version that tries to further explore the level
  public static Tactic explore() {
    return NavTactic.explore(null);
  }

  // Explores to a given location, if location is null then it explores new parts of the level
  public static Tactic explore(CustomVec3D heuristicLocation) {
    return NavAction.explore(heuristicLocation).lift();
  }
}
