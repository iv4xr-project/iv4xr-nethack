package agent.iv4xr;

import agent.navigation.HierarchicalNavigation;
import agent.navigation.NetHackSurface;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.*;
import eu.iv4xr.framework.extensions.pathfinding.Navigatable;
import eu.iv4xr.framework.mainConcepts.Iv4xrAgentState;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import nethack.NetHack;
import nethack.enums.EntityType;
import nethack.object.Entity;
import nethack.object.Level;
import nethack.object.Player;
import nl.uu.cs.aplib.mainConcepts.Environment;
import util.ColoredStringBuilder;
import util.Loggers;

/**
 * Provides an implementation of agent state {@link nl.uu.cs.aplib.mainConcepts.SimpleState}. The
 * state defined here inherits from {@link Iv4xrAgentState}, so it also keeps a historical
 * WorldModel.
 *
 * @author wish
 */
public class AgentState extends Iv4xrAgentState<Void> {
  public HierarchicalNavigation hierarchicalNav;

  @Override
  public AgentEnv env() {
    return (AgentEnv) super.env();
  }

  public NetHack app() {
    return env().app;
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
    setNavGraph();
    return this;
  }

  public WorldEntity auxState() {
    return worldmodel.elements.get("aux");
  }

  private void setNavGraph() {
    NetHackSurface newNav = new NetHackSurface();
    assert hierarchicalNav == null;
    hierarchicalNav = new HierarchicalNavigation(newNav);
  }

  @Override
  public void updateState(String agentId) {
    super.updateState(agentId);
    updateMap();
    updateEntities();
  }

  public NetHackSurface area() {
    return hierarchicalNav.areas.get(NavUtils.levelNr(worldmodel.position));
  }

  private void updateMap() {
    WorldEntity aux = auxState();
    int levelNr = (int) aux.properties.get("levelNr");
    // If detecting a new maze, need to allocate a nav-graph for this maze:
    if (levelNr >= hierarchicalNav.areas.size()) {
      Loggers.AgentLogger.info("Adding a new level at index: %d", levelNr);
      hierarchicalNav.addNextArea(new NetHackSurface());
      var previousLocation =
          NavUtils.loc3(worldmodel.elements.get(Player.ID).getPreviousState().position);
      var currentLocation = NavUtils.loc3(worldmodel.position);

      // TODO: Add edge between surface
    }

    NetHackSurface surface = area();

    Serializable[] changedCoordinates = (Serializable[]) aux.properties.get("changedCoordinates");
    Loggers.AgentLogger.debug("update state with %d new coordinates", changedCoordinates.length);
    List<Tile> updatedTiles = new ArrayList<>();
    List<IntVec2D> toggleBlockingOff = new ArrayList<>();

    IntVec2D playerPos = NavUtils.loc2(worldmodel.position);
    Set<IntVec2D> adjacentCoords =
        new HashSet<>(NavUtils.neighbourCoordinates(playerPos, Level.SIZE, true));

    for (Serializable entry_ : changedCoordinates) {
      Serializable[] entry = (Serializable[]) entry_;
      IntVec2D pos = (IntVec2D) entry[0];
      adjacentCoords.remove(pos);

      EntityType type = (EntityType) entry[1];

      switch (type) {
        case PLAYER:
          if (area().nullTile(pos)) {
            updatedTiles.add(new Unknown(pos));
          } else {
            toggleBlockingOff.add(pos);
          }
          break;
        case WALL:
        case BOULDER:
          updatedTiles.add(new Wall(pos));
          break;
        case CORRIDOR:
          updatedTiles.add(new Corridor(pos));
          break;
        case FLOOR:
          updatedTiles.add(new Floor(pos));
          break;
        case DOOR:
          boolean isOpen = !env().app.level().getEntity(pos).closedDoor();
          updatedTiles.add(new Door(pos, isOpen));
          break;
        case DOORWAY:
          updatedTiles.add(new Doorway(pos));
          break;
        case PRISON_BARS:
          updatedTiles.add(new PrisonBars(pos));
          break;
        case STAIRS_DOWN:
          updatedTiles.add(new Stair(pos, Climbable.ClimbType.Descendable));
          break;
        case STAIRS_UP:
          updatedTiles.add(new Stair(pos, Climbable.ClimbType.Ascendable));
          break;
        case SINK:
          updatedTiles.add(new Sink(pos));
          break;
        default:
          // If the tile has been seen we switch the state to non-blocking.
          // If we don't know the type of the tile, we for now put a tile in its place
          if (area().nullTile(pos)) {
            updatedTiles.add(new Unknown(pos));
          } else if (area().canBeDoor(pos)) {
            if (area().getTile(pos) instanceof Unknown) {
              updatedTiles.add(new Door(pos, true));
            }
            // If the type is more specific than Tile, then don't change anything
          } else {
            toggleBlockingOff.add(pos);
          }
          break;
      }
    }

    // Missed adjacent coords, these are walls
    for (IntVec2D adjacentPos : adjacentCoords) {
      if (surface.nullTile(adjacentPos)) {
        updatedTiles.add(new Wall(adjacentPos));
      }
    }

    surface.updateTiles(updatedTiles, toggleBlockingOff);

    // Clear list of coordinates
    aux.properties.put("changedCoordinates", new Serializable[0]);
  }

  private void updateEntities() {
    // Update visibility cone
    IntVec2D playerPos = NavUtils.loc2(worldmodel.position);
    Level level = env().app.gameState.getLevel();
    HashSet<IntVec2D> visibleCoordinates =
        new HashSet<>(area().VisibleCoordinates(playerPos, level));

    // Remove all entities that are in vision range but can't be seen.
    List<String> idsToRemove = new ArrayList<>();
    Loggers.WOMLogger.info("WOM contains %d elements", worldmodel.elements.size());
    for (WorldEntity we : worldmodel.elements.values()) {
      if (we.position == null) {
        Loggers.WOMLogger.debug("%s [%s]", we.id, we.type);
        continue;
      } else {
        Loggers.WOMLogger.debug(
            "%d <%d,%d> %s [%s]",
            (int) we.position.z, (int) we.position.x, (int) we.position.y, we.id, we.type);
      }
      if (we.id.equals(Player.ID) || we.id.equals("aux")) {
        continue;
      }

      IntVec2D entityPosition = NavUtils.loc2(we.position);
      // If it is not inside the visibility then do not update.
      if (!visibleCoordinates.contains(entityPosition)) {
        continue;
      }

      Entity e = level.getEntity(entityPosition);
      String id = e.createId(entityPosition);
      if (!id.equals(we.id)) {
        Loggers.WOMLogger.debug("REMOVE: %s [%s]", we.id, we.type);
        idsToRemove.add(we.id);
      }
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
  public List<WorldEntity> adjacentEntities(EntityType type, boolean allowDiagonally) {
    List<WorldEntity> ms =
        worldmodel.elements.values().stream()
            .filter(
                e ->
                    Objects.equals(e.type, type.name())
                        && NavUtils.levelNr(worldmodel.position) == NavUtils.levelNr(e.position)
                        && NavUtils.adjacent(
                            NavUtils.toTile(worldmodel.position),
                            NavUtils.toTile(e.position),
                            allowDiagonally))
            .collect(Collectors.toList());

    if (!ms.isEmpty()) {
      Loggers.AgentLogger.debug(
          "Found %d %s nearby (diagonal=%b)", ms.size(), type.name(), allowDiagonally);
    }

    return ms;
  }

  public boolean nextToEntity(EntityType entityType, boolean allowDiagonally) {
    return adjacentEntities(entityType, allowDiagonally).size() > 0;
  }

  public boolean nextToEntity(String entityId, boolean allowDiagonally) {
    Tile p = NavUtils.toTile(env().app.gameState.player.position);
    List<WorldEntity> ms =
        worldmodel.elements.values().stream()
            .filter(
                e ->
                    e.position != null
                        && Objects.equals(e.id, entityId)
                        && NavUtils.adjacent(p, NavUtils.toTile(e.position), allowDiagonally))
            .collect(Collectors.toList());
    return !ms.isEmpty();
  }

  public void render() {
    ColoredStringBuilder csb = new ColoredStringBuilder();
    String[] navigation = area().toString().split(System.lineSeparator());
    String[] game = env().app.gameState.toString().split(System.lineSeparator());
    String[] hierarchicalMap = area().hierarchicalMap.toString().split(System.lineSeparator());

    String formatString =
        String.format(
            "%%-%ds %%-%ds %%-%ds%n", Level.SIZE.width, Level.SIZE.width, Level.SIZE.width);
    int n = Level.SIZE.height;

    csb.appendf(formatString, game[0], "", hierarchicalMap[0]);

    for (int i = 0; i < n; i++) {
      csb.appendf(formatString, game[i + 1], navigation[i], hierarchicalMap[i + 1]);
    }

    csb.appendf(formatString, game[n + 1], "", hierarchicalMap[n + 1]);
    csb.appendf(formatString, game[n + 2], "", hierarchicalMap[n + 2]);
    csb.appendf(formatString, "", "", hierarchicalMap[n + 3]);
    System.out.print(csb);
  }
}
