package agent.navigation.strategy;

import static nl.uu.cs.aplib.AplibEDSL.action;

import agent.iv4xr.AgentState;
import agent.navigation.hpastar.search.Path;
import agent.selector.EntitySelector;
import agent.selector.Selector;
import nethack.object.Entity;
import nl.uu.cs.aplib.mainConcepts.Action;
import util.CustomVec3D;
import util.Loggers;
import util.Sounds;

public class NavAction {
  /** Construct an action that would guide the agent to the given location. */
  public static Action navigateTo(CustomVec3D target) {
    return action("move")
        .do2(
            (AgentState S) ->
                (CustomVec3D nextLoc) -> {
                  Loggers.NavLogger.info("navigateTo %s via %s", target, nextLoc);
                  S.app().move(NavUtils.toDirection(S, nextLoc));
                  return S.getNewWOM();
                })
        .on((AgentState S) -> NavUtils.nextLoc(NavUtils.adjustedFindPath(S, S.loc(), target)));
  }

  // Construct an action that would guide the agent to the target entity.
  public static Action navigateTo(String targetId) {
    return action("move-to")
        .do2(
            (AgentState S) ->
                (CustomVec3D nextLoc) -> {
                  Loggers.NavLogger.info("navigateTo %s via %s", targetId, nextLoc);
                  S.app().move(NavUtils.toDirection(S, nextLoc));
                  return S.getNewWOM();
                })
        .on(
            (AgentState S) -> {
              Entity e = S.worldmodel.getElement(targetId);
              if (e == null) {
                Loggers.NavLogger.debug("Entity does not exist");
                return null;
              }
              return NavUtils.nextLoc(NavUtils.adjustedFindPath(S, S.loc(), e.loc));
            });
  }

  public static Action navigateTo() {
    return action("move-to")
        .do2(
            (AgentState S) ->
                (CustomVec3D nextLoc) -> {
                  Loggers.NavLogger.info("navigateTo ? via %s", nextLoc);
                  S.app().move(NavUtils.toDirection(S, nextLoc));
                  return S.getNewWOM();
                });
  }

  // Construct an action that would guide the agent to the target entity.
  public static Action navigateNextTo(String targetId, boolean allowDiagonally) {
    return action("move-to")
        .do2(
            (AgentState S) ->
                (CustomVec3D nextLoc) -> {
                  Loggers.NavLogger.info("navigateNextTo %s via %s", targetId, nextLoc);
                  S.app().move(NavUtils.toDirection(S, nextLoc));
                  return S.getNewWOM();
                })
        .on(
            (AgentState S) -> {
              Entity e = S.worldmodel.getElement(targetId);
              if (e == null) {
                Loggers.NavLogger.debug("Element not found");
                return null;
              }
              EntitySelector nextToEntity =
                  new EntitySelector()
                      .selectionType(Selector.SelectionType.ADJACENT)
                      .predicate((tempE, ignore) -> tempE.getId().equals(targetId));
              Entity adjacentEntity = nextToEntity.apply(S.worldmodel.getCurrentElements(), S);
              if (adjacentEntity != null) {
                Loggers.NavLogger.debug("Next to item id:%s", targetId);
                return null;
              }
              return NavUtils.nextLoc(NavUtils.adjustedFindPath(S, S.loc(), e.loc));
            });
  }

  /** Construct an action that would explore the world, in the direction of the given location. */
  public static Action explore(CustomVec3D heuristicLocation) {
    return action("explore")
        .do2(
            (AgentState S) ->
                (CustomVec3D nextLoc) -> {
                  Sounds.explore();
                  S.app().move(NavUtils.toDirection(S, nextLoc));
                  return S.getNewWOM();
                })
        .on(
            (AgentState S) -> {
              Path<CustomVec3D> path;
              if (heuristicLocation == null) {
                path = S.hierarchicalNav().explore(S.loc());
              } else {
                path = S.hierarchicalNav().explore(S.loc(), heuristicLocation);
              }

              if (path == null || path.nextNode() == null) {
                return null;
              }

              CustomVec3D nextLoc = path.nextNode();
              Loggers.NavLogger.info("explore to %s via %s: %s", path.destination(), nextLoc, path);
              return nextLoc;
            });
  }
}
