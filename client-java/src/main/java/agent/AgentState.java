package agent;

import agent.navigation.NavUtils;
import eu.iv4xr.framework.extensions.pathfinding.LayeredAreasNavigation;
import eu.iv4xr.framework.extensions.pathfinding.Navigatable;
import eu.iv4xr.framework.mainConcepts.Iv4xrAgentState;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.IntVec2D;
import nethack.NetHack;
import nethack.object.Entity;
import nethack.enums.EntityType;
import nethack.object.Level;
import nethack.object.Player;
import agent.navigation.NetHackSurface;
import agent.navigation.surface.*;
import nl.uu.cs.aplib.mainConcepts.Environment;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides an implementation of agent state
 * {@link nl.uu.cs.aplib.mainConcepts.SimpleState}. The state defined here
 * inherits from {@link Iv4xrAgentState}, so it also keeps a historical
 * WorldModel.
 *
 * @author wish
 */
public class AgentState extends Iv4xrAgentState<Void> {
    static final Logger agentLogger = LogManager.getLogger(AgentLoggers.AgentLogger);
    static final Logger WOMLogger = LogManager.getLogger(AgentLoggers.WOMLogger);
    public LayeredAreasNavigation<Tile, NetHackSurface> multiLayerNav;

    @Override
    public AgentEnv env() {
        return (AgentEnv) super.env();
    }

    public NetHack app() { return env().app; }

    /**
     * We are not going to keep a Nav-graph, but will instead keep a
     * layered-nav-graphs.
     */
    @Override
    public Navigatable<Void> worldNavigation() {
        throw new UnsupportedOperationException();
    }

    /**
     * We are not going to keep a Nav-graph, but will instead keep a
     * layered-nav-graphs.
     */
    @Override
    public AgentState setWorldNavigation(Navigatable<Void> nav) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AgentState setEnvironment(Environment env) {
        super.setEnvironment(env);
        multiLayerNav = new LayeredAreasNavigation<>();
        multiLayerNav.setPerfectMemoryPathfinding(true);
        addNewNavGraph(false);
        return this;
    }

    public WorldEntity auxState() {
        return worldmodel.elements.get("aux");
    }

    private void addNewNavGraph(boolean withPortal) {
        NetHackSurface newNav = new NetHackSurface();

        if (withPortal) {
            Player player = env().app.gameState.player;
            Tile previousLevelStairCase = new Tile(player.previousPosition2D);
            Tile nextLevelStairCase = new Tile(player.position2D);
            multiLayerNav.addNextArea(newNav, previousLevelStairCase, nextLevelStairCase, true);
        } else {
            multiLayerNav.addNextArea(newNav, null, null, false);
        }
    }

    @Override
    public void updateState(String agentId) {
        super.updateState(agentId);
        updateMap();
        updateEntities();
    }

    public NetHackSurface area() {
        return multiLayerNav.areas.get((int)worldmodel.position.z);
    }

    private void updateMap() {
        WorldEntity aux = auxState();
        int levelNr = (int)aux.properties.get("levelId");

        Serializable[] changedCoordinates = (Serializable[]) aux.properties.get("changedCoordinates");
        agentLogger.debug(String.format("update state with %d new coordinates", changedCoordinates.length));
        for (Serializable entry_ : changedCoordinates) {
            Serializable[] entry = (Serializable[]) entry_;
            IntVec2D pos = (IntVec2D) entry[0];
            EntityType type = (EntityType) entry[1];

            // If detecting a new maze, need to allocate a nav-graph for this maze:
            if (levelNr >= multiLayerNav.areas.size()) {
                agentLogger.info(String.format("Adding a new level: %s", levelNr));
                addNewNavGraph(true);
            }

            switch (type) {
                case WALL:
                case BOULDER:
                    multiLayerNav.addObstacle(new Pair<>(levelNr, new Wall(pos)));
                    break;
                case CORRIDOR:
                    multiLayerNav.removeObstacle(new Pair<>(levelNr, new Corridor(pos)));
                    break;
                case FLOOR:
                    multiLayerNav.removeObstacle(new Pair<>(levelNr, new Floor(pos)));
                    break;
                case DOOR:
                    boolean isOpen = !env().app.gameState.level().getEntity(pos).closedDoor();
                    multiLayerNav.addObstacle(new Pair<>(levelNr, new Door(pos, isOpen)));
                    break;
                case DOORWAY:
                    multiLayerNav.removeObstacle(new Pair<>(levelNr, new Doorway(pos)));
                    break;
                case PRISON_BARS:
                    multiLayerNav.addObstacle(new Pair<>(levelNr, new PrisonBars(pos)));
                    break;
                default:
                    // If the tile has been seen we switch the state to non-blocking.
                    // If we don't know the type of the tile, we for now put a tile in its place
                    if (multiLayerNav.areas.get(levelNr).hasTile(pos)) {
                        multiLayerNav.toggleBlockingOff(new Pair<>(levelNr, new Tile(pos)));
                    } else {
                        multiLayerNav.removeObstacle(new Pair<>(levelNr, new Tile(pos)));
                    }
                    break;
            }

            // Lastly mark it as seen
            multiLayerNav.markAsSeen(new Pair<>(levelNr, new Tile(pos)));
        }

        NetHackSurface navGraph = multiLayerNav.areas.get(levelNr);
        IntVec2D playerPos = NavUtils.loc2(worldmodel.position);
        // Each entity that is next to the agent which is void is a wall
        IntVec2D[] adjacentCoords = NavUtils.neighbourCoordinates(playerPos);
        for (IntVec2D adjacentPos: adjacentCoords) {
            if (!navGraph.hasTile(adjacentPos)) {
                multiLayerNav.addObstacle(new Pair<>(levelNr, new Wall(adjacentPos)));
                multiLayerNav.markAsSeen(new Pair<>(levelNr, new Tile(adjacentPos)));
            }
        }
    }

    private void updateEntities() {
        // Update visibility cone
        WorldEntity aux = auxState();
        int levelNr = (int)aux.properties.get("levelId");
        NetHackSurface navGraph = multiLayerNav.areas.get(levelNr);
        IntVec2D playerPos = NavUtils.loc2(worldmodel.position);
        Level level = env().app.gameState.level();
        HashSet<IntVec2D> visibleCoordinates = new HashSet<>(navGraph.VisibleCoordinates(playerPos, level));

        // Remove all entities that are in vision range but can't be seen.
        List<String> idsToRemove = new ArrayList<>();
        WOMLogger.info(String.format("WOM contains %d elements", worldmodel.elements.size()));
        for (WorldEntity we: worldmodel.elements.values()) {
            if (we.position == null) {
                WOMLogger.debug(String.format("%s [%s]", we.id, we.type));
                continue;
            } else {
                WOMLogger.debug(String.format("%d <%d,%d> %s [%s]", (int)we.position.z, (int)we.position.x, (int)we.position.y, we.id, we.type));
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
            e.assignId(entityPosition);
            if (!e.id.equals(we.id)) {
                WOMLogger.debug(String.format("REMOVE: %s [%s]", we.id, we.type));
                idsToRemove.add(we.id);
            }
        }

        // Separate loop since it changes the map
        for (String id: idsToRemove) {
            worldmodel.elements.remove(id);
        }
    }

    /**
     * Return the game status (as registered in this state).
     */
    public boolean gameStatus() {
        WorldEntity aux = worldmodel.elements.get("aux");
        return (boolean) aux.properties.get("status");
    }

    /**
     * Check if the agent that owns this state is alive in the game (its hp>0).
     */
    public boolean agentIsAlive() {
        WorldEntity a = worldmodel.elements.get(worldmodel.agentId);
        if (a == null) {
            throw new IllegalArgumentException();
        }
        Integer hp = (Integer) a.properties.get("hp");
        if (hp == null) {
            throw new IllegalArgumentException();
        }
        return hp > 0;
    }

    /**
     * Return a list of entities with a certain type which are currently adjacent to the agent that owns
     * this state.
     */
    public List<WorldEntity> adjacentEntities(EntityType type, boolean allowDiagonally) {
        WorldEntity player = worldmodel.elements.get("player");
        Tile p = NavUtils.toTile(player.position);

        List<WorldEntity> ms = worldmodel.elements
                .values().stream().filter(e -> Objects.equals(e.type, type.name())
                        && NavUtils.levelNr(player) == NavUtils.levelNr(e) && NavUtils.adjacent(p, NavUtils.toTile(e.position), allowDiagonally))
                .collect(Collectors.toList());

        if (ms.size() > 0) {
            agentLogger.debug(String.format("Found %d %s nearby (diagonal=%b)", ms.size(), type.name(), allowDiagonally));
        }

        return ms;
    }

    public boolean nextToEntity(EntityType entityType, boolean allowDiagonally) {
        return adjacentEntities(entityType, allowDiagonally).size() > 0;
    }

    public boolean nextToEntity(String entityId, boolean allowDiagonally) {
        Tile p = NavUtils.toTile(env().app.gameState.player.position);
        List<WorldEntity> ms = worldmodel.elements.values().stream().filter(e -> e.position != null && Objects.equals(e.id, entityId) && NavUtils.adjacent(p, NavUtils.toTile(e.position), allowDiagonally)).collect(Collectors.toList());
        return ms.size() > 0;
    }

    public void render() {
        NetHackSurface layer = multiLayerNav.areas.get(env().app.gameState.stats.zeroIndexLevelNumber);

        String[] navigation = layer.toString().split(System.lineSeparator());
        String[] game = env().app.gameState.toString().split(System.lineSeparator());

        System.out.println(game[0]);

        for (int i = 0; i < Level.HEIGHT; i++) {
            System.out.println(game[i + 1] + " " + navigation[i]);
        }

        System.out.println(game[Level.HEIGHT + 1]);
        System.out.println(game[Level.HEIGHT + 2]);
    }
}
