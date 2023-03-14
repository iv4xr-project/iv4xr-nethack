package agent.navigation.strategy;

import static nl.uu.cs.aplib.AplibEDSL.action;

import agent.iv4xr.AgentState;
import agent.navigation.surface.Tile;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.Vec3;
import java.util.List;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.utils.Pair;
import util.Loggers;
import util.Sounds;

public class NavAction {
  /** Construct an action that would guide the agent to the given location. */
  public static Action navigateTo(int levelNr, int x, int y) {
    return action("move-to")
        .do2(
            (AgentState S) ->
                (Pair<Integer, Tile> nextTile) -> {
                  Loggers.NavLogger.info("navigateTo %d (%d, %d) via %s", levelNr, x, y, nextTile);
                  return new Pair<>(S, NavUtils.moveTo(S, nextTile));
                })
        .on(
            (AgentState S) ->
                NavUtils.nextTile(
                    NavUtils.adjustedFindPath(
                        S, NavUtils.loc3(S.worldmodel.position), NavUtils.loc3(levelNr, x, y))));
  }

  // Construct an action that would guide the agent to the target entity.
  public static Action navigateTo(String targetId) {
    return action("move-to")
        .do2(
            (AgentState S) ->
                (Pair<Integer, Tile> nextTile) -> {
                  Loggers.NavLogger.info("navigateTo %s via %s", targetId, nextTile);
                  return new Pair<>(S, NavUtils.moveTo(S, nextTile));
                })
        .on(
            (AgentState S) -> {
              WorldEntity e = S.worldmodel.elements.get(targetId);
              if (e == null) {
                Loggers.NavLogger.debug("Entity does not exist");
                return null;
              }
              return NavUtils.nextTile(
                  NavUtils.adjustedFindPath(
                      S, NavUtils.loc3(S.worldmodel.position), NavUtils.loc3(e.position)));
            });
  }

  public static Action navigateTo() {
    return action("move-to")
        .do2(
            (AgentState S) ->
                (Pair<Integer, Tile> nextTile) -> {
                  Loggers.NavLogger.info("navigateTo ? via %s", nextTile);
                  return new Pair<>(S, NavUtils.moveTo(S, nextTile));
                });
  }

  // Construct an action that would guide the agent to the target entity.
  public static Action navigateNextTo(String targetId, boolean allowDiagonally) {
    return action("move-to")
        .do2(
            (AgentState S) ->
                (Pair<Integer, Tile> nextTile) -> {
                  Loggers.NavLogger.info("navigateNextTo %s via %s", targetId, nextTile);
                  return new Pair<>(S, NavUtils.moveTo(S, nextTile));
                })
        .on(
            (AgentState S) -> {
              WorldEntity e = S.worldmodel.elements.get(targetId);
              if (e == null) {
                Loggers.NavLogger.debug("Element not found");
                return null;
              }
              if (S.nextToEntity(targetId, allowDiagonally)) {
                Loggers.NavLogger.debug("Next to item id:%s", targetId);
                return null;
              }
              return NavUtils.nextTile(
                  NavUtils.adjustedFindPath(
                      S, NavUtils.loc3(S.worldmodel.position), NavUtils.loc3(e.position)));
            });
  }

  /** Construct an action that would explore the world, in the direction of the given location. */
  public static Action explore(Pair<Integer, Tile> heuristicLocation) {
    return action("explore")
        .do2(
            (AgentState S) ->
                (Pair<Integer, Tile> nextTile) -> {
                  Sounds.explore();
                  return new Pair<>(S, NavUtils.moveTo(S, nextTile));
                })
        .on(
            (AgentState S) -> {
              Vec3 agentPos = S.worldmodel.position;
              List<Pair<Integer, Tile>> path;
              if (heuristicLocation == null) {
                path = S.hierarchicalNav.explore(NavUtils.loc3(agentPos));
              } else {
                path = S.hierarchicalNav.explore(NavUtils.loc3(agentPos), heuristicLocation);
              }
              Pair<Integer, Tile> nextTile = NavUtils.nextTile(path);
              if (nextTile == null) {
                return null;
              }
              Loggers.NavLogger.info(
                  "explore to %s via %s", path.get(path.size() - 1), nextTile, path);
              return nextTile;
            });
  }
}
