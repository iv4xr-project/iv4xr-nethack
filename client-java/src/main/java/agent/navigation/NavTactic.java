package agent.navigation;

import agent.AgentLoggers;
import agent.AgentState;
import agent.navigation.surface.Tile;
import agent.selector.EntitySelector;
import agent.selector.TileSelector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.IntVec2D;
import eu.iv4xr.framework.spatial.Vec3;
import java.util.ArrayList;
import java.util.List;
import nl.uu.cs.aplib.mainConcepts.SimpleState;
import nl.uu.cs.aplib.mainConcepts.Tactic;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NavTactic {
  static final Logger logger = LogManager.getLogger(AgentLoggers.NavLogger);

  public static Tactic navigateTo(Pair<Integer, Tile> location) {
    return NavAction.navigateTo(location.fst, location.snd.pos.x, location.snd.pos.y).lift();
  }

  // Construct a tactic that would guide the agent to a tile to the target entity.
  public static Tactic navigateTo(String targetId) {
    return NavAction.navigateTo(targetId).lift();
  }

  public static Tactic navigateToWorldEntity(EntitySelector entitySelector) {
    return NavAction.navigateTo()
        .on(
            (AgentState S) -> {
              Vec3 agentPos = S.worldmodel.position;
              WorldEntity e =
                  entitySelector.apply(new ArrayList<>(S.worldmodel.elements.values()), S);
              if (e == null) {
                return null;
              } else if (agentPos.equals(e.position)) {
                return null;
              }
              Tile nextTile =
                  NavUtils.nextTile(
                      NavUtils.adjustedFindPath(
                          S, NavUtils.loc3(agentPos), NavUtils.loc3(e.position)));
              if (nextTile == null) {
                return null;
              }
              logger.debug(
                  String.format(">>> navigateToWorldEntity (%s) via %s", entitySelector, nextTile));
              return nextTile;
            })
        .lift();
  }

  public static Tactic navigateToTile(TileSelector tileSelector) {
    return NavAction.navigateTo()
        .on(
            (AgentState S) -> {
              List<Tile> tiles = new ArrayList<>();
              for (Tile[] row : S.area().tiles) {
                for (Tile tile : row) {
                  if (tile != null) {
                    tiles.add(tile);
                  }
                }
              }
              Tile t = tileSelector.apply(tiles, S);
              if (t == null) {
                logger.debug("Tile does not exist in level");
                return null;
              }
              Vec3 agentPos = S.worldmodel.position;
              Tile nextTile =
                  NavUtils.nextTile(
                      NavUtils.adjustedFindPath(
                          S, NavUtils.loc3(agentPos), NavUtils.loc3((int) agentPos.z, t.pos)));
              if (nextTile == null) {
                return null;
              }
              logger.debug(String.format(">>> navigateToTile (%s) via %s", tileSelector, nextTile));
              return nextTile;
            })
        .lift();
  }

  public static Tactic navigateNextToTile(TileSelector tileSelector, boolean allowDiagonal) {
    return NavAction.navigateTo()
        .on(
            (AgentState S) -> {
              List<Tile> tiles = new ArrayList<>();
              for (Tile[] row : S.area().tiles) {
                for (Tile tile : row) {
                  if (tile != null) {
                    tiles.add(tile);
                  }
                }
              }
              Tile t = tileSelector.apply(tiles, S);
              if (t == null) {
                logger.debug("Tile does not exist in level");
                return null;
              }
              Vec3 agentPos = S.worldmodel.position;
              NetHackSurface surface = S.area();
              List<Pair<Integer, Tile>> path = null;
              for (IntVec2D pos : NavUtils.neighbourCoordinates(t.pos)) {
                if (pos.equals(NavUtils.loc2(agentPos))) {
                  return null;
                }
                if (!surface.hasTile(pos)) {
                  continue;
                }
                if (surface.isBlocking(pos)) {
                  continue;
                }

                List<Pair<Integer, Tile>> pathToNeighbour =
                    NavUtils.adjustedFindPath(
                        S, NavUtils.loc3(agentPos), NavUtils.loc3((int) agentPos.z, pos));
                if (pathToNeighbour == null) {
                  continue;
                }
                if (path == null || pathToNeighbour.size() < path.size()) {
                  path = pathToNeighbour;
                }
              }

              Tile nextTile = NavUtils.nextTile(path);
              if (nextTile == null) {
                return null;
              }
              logger.debug(
                  String.format(
                      ">>> navigateNextToTile (%s) via %s (allowdiagonal=%b)",
                      tileSelector, nextTile, allowDiagonal));
              return nextTile;
            })
        .lift();
  }

  // Construct a tactic that would guide the agent to a tile adjacent to the location.
  public static Tactic navigateNextTo(Pair<Integer, Tile> location, boolean allowDiagonally) {
    return NavAction.navigateTo(location.fst, location.snd.pos.x, location.snd.pos.y)
        .on_(
            (AgentState S) -> {
              return !NavUtils.adjacent(
                  new Tile(NavUtils.loc2(S.worldmodel.position)), location.snd, allowDiagonally);
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
  public static Tactic explore(Pair<Integer, Tile> heuristicLocation) {
    return NavAction.explore(heuristicLocation).lift();
  }
}
