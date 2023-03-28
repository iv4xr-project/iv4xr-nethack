package agent.navigation.strategy;

import static nl.uu.cs.aplib.AplibEDSL.action;

import agent.iv4xr.AgentState;
import agent.navigation.hpastar.search.Path;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.utils.Pair;
import util.CustomVec3D;
import util.Loggers;
import util.Sounds;

public class NavAction {
  /** Construct an action that would guide the agent to the given location. */
  public static Action navigateTo(CustomVec3D target) {
    return action("move-to")
        .do2(
            (AgentState S) ->
                (CustomVec3D nextLoc) -> {
                  Loggers.NavLogger.info("navigateTo %s via %s", target, nextLoc);
                  return new Pair<>(S, NavUtils.moveTo(S, nextLoc));
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
                  return new Pair<>(S, NavUtils.moveTo(S, nextLoc));
                })
        .on(
            (AgentState S) -> {
              WorldEntity e = S.worldmodel.elements.get(targetId);
              if (e == null) {
                Loggers.NavLogger.debug("Entity does not exist");
                return null;
              }
              return NavUtils.nextLoc(
                  NavUtils.adjustedFindPath(S, S.loc(), new CustomVec3D(e.position)));
            });
  }

  public static Action navigateTo() {
    return action("move-to")
        .do2(
            (AgentState S) ->
                (CustomVec3D nextLoc) -> {
                  Loggers.NavLogger.info("navigateTo ? via %s", nextLoc);
                  return new Pair<>(S, NavUtils.moveTo(S, nextLoc));
                });
  }

  // Construct an action that would guide the agent to the target entity.
  public static Action navigateNextTo(String targetId, boolean allowDiagonally) {
    return action("move-to")
        .do2(
            (AgentState S) ->
                (CustomVec3D nextLoc) -> {
                  Loggers.NavLogger.info("navigateNextTo %s via %s", targetId, nextLoc);
                  return new Pair<>(S, NavUtils.moveTo(S, nextLoc));
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
              return NavUtils.nextLoc(
                  NavUtils.adjustedFindPath(S, S.loc(), new CustomVec3D(e.position)));
            });
  }

  /** Construct an action that would explore the world, in the direction of the given location. */
  public static Action explore(CustomVec3D heuristicLocation) {
    return action("explore")
        .do2(
            (AgentState S) ->
                (CustomVec3D nextLoc) -> {
                  Sounds.explore();
                  return new Pair<>(S, NavUtils.moveTo(S, nextLoc));
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
