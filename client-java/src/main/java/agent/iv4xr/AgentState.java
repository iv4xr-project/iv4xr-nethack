package agent.iv4xr;

import agent.navigation.HierarchicalNavigation;
import agent.navigation.strategy.NavUtils;
import eu.iv4xr.framework.extensions.pathfinding.Navigatable;
import eu.iv4xr.framework.mainConcepts.Iv4xrAgentState;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import java.util.*;
import java.util.stream.Collectors;
import nethack.NetHack;
import nethack.enums.SymbolType;
import nethack.object.Player;
import nethack.object.Turn;
import nethack.world.Level;
import nethack.world.Surface;
import nl.uu.cs.aplib.mainConcepts.Environment;
import util.ColoredStringBuilder;
import util.CustomVec2D;
import util.CustomVec3D;
import util.Loggers;

/**
 * Provides an implementation of agent state {@link nl.uu.cs.aplib.mainConcepts.SimpleState}. The
 * state defined here inherits from {@link Iv4xrAgentState}, so it also keeps a historical
 * WorldModel.
 *
 * @author wish
 */
public class AgentState extends Iv4xrAgentState<Void> {
  Turn previousTurn;

  @Override
  public AgentEnv env() {
    return (AgentEnv) super.env();
  }

  public NetHack app() {
    return env().app;
  }

  public Surface area() {
    return app().gameState.getLevel().surface;
  }

  public List<WorldEntity> getWorldEntities() {
    return new ArrayList<>(worldmodel.elements.values());
  }

  public HierarchicalNavigation hierarchicalNav() {
    return app().gameState.dungeon.hierarchicalNav;
  }

  public CustomVec3D loc() {
    return new CustomVec3D(worldmodel.position);
  }

  /** We are not going to keep a Nav-graph, but will instead keep a layered-nav-graphs. */
  @Override
  public Navigatable<Void> worldNavigation() {
    throw new UnsupportedOperationException();
  }

  /** We are not going to keep a Nav-graph, but will instead keep a layered-nav-graphs. */
  @Override
  public AgentState setWorldNavigation(Navigatable<Void> nav) {
    throw new UnsupportedOperationException();
  }

  @Override
  public AgentState setEnvironment(Environment env) {
    super.setEnvironment(env);
    return this;
  }

  public WorldEntity auxState() {
    return worldmodel.elements.get("aux");
  }

  @Override
  public void updateState(String agentId) {
    // Don't update the GameState if it is terminated
    if (app().gameState.done) {
      return;
    }

    // Turn did not update
    Turn currentTurn = app().gameState.stats.turn;
    if (previousTurn != null && previousTurn.equals(currentTurn)) {
      return;
    }

    super.updateState(agentId);
    updateEntities();
    previousTurn = currentTurn;
  }

  private void updateEntities() {
    // Update visibility cone
    CustomVec2D playerPos = NavUtils.loc2(worldmodel.position);
    Level level = env().app.gameState.getLevel();
    Set<CustomVec2D> visibleCoordinates = level.visibleCoordinates;

    // Remove all entities that are in vision range but can't be seen.
    List<String> idsToRemove = new ArrayList<>();
    Loggers.WOMLogger.info("WOM contains %d elements", worldmodel.elements.size());
    for (WorldEntity we : worldmodel.elements.values()) {
      if (we.position == null) {
        Loggers.WOMLogger.debug("%s [%s]", we.id, we.type);
        continue;
      } else {
        Loggers.WOMLogger.debug("%s %s [%s]", new CustomVec3D(we.position), we.id, we.type);
      }
      if (we.id.equals(Player.ID) || we.id.equals("aux")) {
        continue;
      }

      CustomVec2D entityPosition = NavUtils.loc2(we.position);
      // If it is not inside the visibility then do not update.
      if (!visibleCoordinates.contains(entityPosition)) {
        continue;
      }

      //      Symbol e = level.getSymbol(entityPosition);
      //      if (e == null) {
      //        continue;
      //      }
      //
      //      String id = e.createId(entityPosition);
      //      if (!id.equals(we.id)) {
      //        Loggers.WOMLogger.debug("REMOVE: %s [%s]", we.id, we.type);
      //        idsToRemove.add(we.id);
      //      }
    }

    // Separate loop since it changes the map
    for (String id : idsToRemove) {
      worldmodel.elements.remove(id);
    }
  }

  /** Return the game status (as registered in this state). */
  public boolean gameStatus() {
    WorldEntity aux = worldmodel.elements.get("aux");
    return (boolean) aux.properties.get("status");
  }

  /** Check if the agent that owns this state is alive in the game (its hp>0). */
  public boolean agentIsAlive() {
    WorldEntity a = worldmodel.elements.get(worldmodel.agentId);
    assert a != null;
    Integer hp = (Integer) a.properties.get("hp");
    assert hp != null;
    return hp > 0;
  }

  /**
   * Return a list of entities with a certain type which are currently adjacent to the agent that
   * owns this state.
   */
  public List<WorldEntity> adjacentEntities(SymbolType type, boolean allowDiagonally) {
    CustomVec3D agentLoc = loc();
    List<WorldEntity> ms =
        worldmodel.elements.values().stream()
            .filter(
                e ->
                    Objects.equals(e.type, type.name())
                        && NavUtils.levelNr(worldmodel.position) == NavUtils.levelNr(e.position)
                        && CustomVec3D.adjacent(
                            agentLoc, new CustomVec3D(e.position), allowDiagonally))
            .collect(Collectors.toList());

    if (!ms.isEmpty()) {
      Loggers.AgentLogger.debug(
          "Found %d %s nearby (diagonal=%b)", ms.size(), type.name(), allowDiagonally);
    }

    return ms;
  }

  public boolean nextToEntity(SymbolType symbolType, boolean allowDiagonally) {
    return !adjacentEntities(symbolType, allowDiagonally).isEmpty();
  }

  public boolean nextToEntity(String entityId, boolean allowDiagonally) {
    CustomVec3D playerLoc = loc();
    List<WorldEntity> ms =
        worldmodel.elements.values().stream()
            .filter(
                e ->
                    e.position != null
                        && Objects.equals(e.id, entityId)
                        && CustomVec3D.adjacent(
                            playerLoc, new CustomVec3D(e.position), allowDiagonally))
            .collect(Collectors.toList());
    return !ms.isEmpty();
  }

  public void render() {
    ColoredStringBuilder csb = new ColoredStringBuilder();
    String[] navigation = area().toString().split(System.lineSeparator());
    String[] game = env().app.gameState.toString().split(System.lineSeparator());
    String[] hierarchicalMap = area().hierarchicalMap.toString().split(System.lineSeparator());

    String tripleFormatString =
        String.format(
            "%%-%ds %%-%ds %%-%ds%n", Level.SIZE.width, Level.SIZE.width, Level.SIZE.width);
    String doubleFormatString =
        String.format("%%-%ds %%-%ds%n", 2 * Level.SIZE.width + 1, Level.SIZE.width);
    int n = Level.SIZE.height;

    csb.appendf(doubleFormatString, game[0], hierarchicalMap[0]);

    for (int i = 0; i < n; i++) {
      csb.appendf(tripleFormatString, game[i + 1], navigation[i], hierarchicalMap[i + 1]);
    }

    csb.appendf(doubleFormatString, game[n + 1], hierarchicalMap[n + 1]);
    csb.appendf(
        String.format("%%-%ds %%-%ds%n", Level.SIZE.width * 2 + 40, Level.SIZE.width),
        game[n + 2],
        hierarchicalMap[n + 2]);
    csb.appendf(tripleFormatString, "", "", hierarchicalMap[n + 3]);
    System.out.print(csb);
  }
}
