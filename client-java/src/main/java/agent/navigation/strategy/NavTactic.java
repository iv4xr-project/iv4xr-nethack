package agent.navigation.strategy;

import static nl.uu.cs.aplib.AplibEDSL.action;

import agent.iv4xr.AgentState;
import agent.navigation.hpastar.search.Path;
import agent.navigation.surface.Tile;
import agent.selector.EntitySelector;
import agent.selector.TileSelector;
import java.util.List;
import java.util.function.Predicate;
import nethack.object.Command;
import nethack.object.Entity;
import nethack.world.Level;
import nl.uu.cs.aplib.mainConcepts.SimpleState;
import nl.uu.cs.aplib.mainConcepts.Tactic;
import nl.uu.cs.aplib.utils.Pair;
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
              Entity e = entitySelector.apply(S.app().level().entities, S);
              if (e == null) {
                return null;
              }
              Path<CustomVec3D> path = NavUtils.adjustedFindPath(S, S.loc(), e.loc);
              if (path == null || path.nextNode() == null) {
                return null;
              }
              CustomVec3D nextLoc = path.nextNode();
              Loggers.NavLogger.debug(
                  String.format("navigateToWorldEntity (%s) via %s", entitySelector, nextLoc));
              return nextLoc;
            })
        .lift();
  }

  public static Tactic interactWorldEntity(EntitySelector entitySelector, List<Command> commands) {
    return interactWorldEntity(entitySelector, commands, null);
  }

  public static Tactic interactWorldEntity(
      EntitySelector entitySelector, List<Command> commands, Predicate<AgentState> on) {
    return action(String.format("navAndInteract %s %s", entitySelector, commands))
        .do2(
            (AgentState S) ->
                (Path<CustomVec3D> path) -> {
                  if (path.atLocation()) {
                    S.app().step(commands);
                    return S.getNewWOM();
                  } else {
                    return new Pair<>(S, NavUtils.moveTo(S, path.nextNode()));
                  }
                })
        .on(
            (AgentState S) -> {
              if (on != null && !on.test(S)) {
                return null;
              }
              Entity e = entitySelector.apply(S.app().level().entities, S);
              if (e == null) {
                return null;
              }
              return NavUtils.adjustedFindPath(S, S.loc(), e.loc);
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

              Path<CustomVec3D> path = NavUtils.adjustedFindPath(S, S.loc(), tile.loc);
              if (path == null || path.nextNode() == null) {
                return null;
              }
              Loggers.NavLogger.debug("navigateToTile (%s) via %s", tileSelector, path.nextNode());
              return path.nextNode();
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
              List<CustomVec2D> neighbours =
                  NavUtils.neighbourCoordinates(tile.pos, Level.SIZE, allowDiagonal);
              List<CustomVec3D> targets = NavUtils.addLevelNr(neighbours, tile.loc.lvl);
              Path<CustomVec3D> path = S.hierarchicalNav().findShortestPath(agentLoc, targets);
              if (path == null || path.atLocation()) {
                return null;
              }

              CustomVec3D nextLoc = path.nextNode();
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
        .on_((AgentState S) -> !CustomVec3D.adjacent(S.loc(), location, allowDiagonally))
        .lift();
  }

  // Construct a tactic that would guide the agent to a tile adjacent to the target entity.
  public static Tactic navigateNextTo(String targetId, boolean allowDiagonally) {
    return NavAction.navigateNextTo(targetId, allowDiagonally).lift();
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
