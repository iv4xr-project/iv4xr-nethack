package agent.navigation;

import static nl.uu.cs.aplib.AplibEDSL.action;

import agent.AgentLoggers;
import agent.iv4xr.AgentState;
import agent.navigation.surface.Tile;
import agent.util.Sounds;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NavAction {
  static final Logger logger = LogManager.getLogger(AgentLoggers.NavLogger);

  /** Construct an action that would guide the agent to the given location. */
  public static Action navigateTo(int levelNr, int x, int y) {
    return action("move-to")
        .do2(
            (AgentState S) ->
                (Tile nextTile) -> {
                  logger.info(
                      String.format("navigateTo %d (%d, %d) via %s", levelNr, x, y, nextTile));
                  WorldModel newwom = NavUtils.moveTo(S, nextTile);
                  return new Pair<>(S, newwom);
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
                (Tile nextTile) -> {
                  logger.info(String.format("navigateTo %s via %s", targetId, nextTile));
                  WorldModel newwom = NavUtils.moveTo(S, nextTile);
                  return new Pair<>(S, newwom);
                })
        .on(
            (AgentState S) -> {
              WorldEntity e = S.worldmodel.elements.get(targetId);
              if (e == null) {
                logger.debug("Entity does not exist");
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
                (Tile nextTile) -> {
                  logger.info(String.format("navigateTo ? via %s", nextTile));
                  WorldModel newwom = NavUtils.moveTo(S, nextTile);
                  return new Pair<>(S, newwom);
                });
  }

  // Construct an action that would guide the agent to to the target entity.
  public static Action navigateNextTo(String targetId, boolean allowDiagonally) {
    return action("move-to")
        .do2(
            (AgentState S) ->
                (Tile nextTile) -> {
                  logger.info(String.format("navigateNextTo %s via %s", targetId, nextTile));
                  WorldModel newwom = NavUtils.moveTo(S, nextTile);
                  return new Pair<>(S, newwom);
                })
        .on(
            (AgentState S) -> {
              WorldEntity e = S.worldmodel.elements.get(targetId);
              if (e == null) {
                logger.debug("Element not found");
                return null;
              }
              if (S.nextToEntity(targetId, allowDiagonally)) {
                logger.debug(String.format("Next to item id:%s", targetId));
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
                (Tile nextTile) -> {
                  Sounds.explore();
                  logger.info(String.format("explore to %s via %s", heuristicLocation, nextTile));
                  WorldModel newwom = NavUtils.moveTo(S, nextTile);
                  return new Pair<>(S, newwom);
                })
        .on(
            (AgentState S) -> {
              Vec3 agentPos = S.worldmodel.position;
              if (heuristicLocation == null) {
                return NavUtils.nextTile(S.multiLayerNav.explore(NavUtils.loc3(agentPos)));
              } else {
                return NavUtils.nextTile(
                    S.multiLayerNav.explore(NavUtils.loc3(agentPos), heuristicLocation));
              }
            });
  }
}
