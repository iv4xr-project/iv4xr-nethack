package nethack.agent;

import eu.iv4xr.framework.extensions.pathfinding.LayeredAreasNavigation;
import eu.iv4xr.framework.extensions.pathfinding.Navigatable;
import eu.iv4xr.framework.mainConcepts.Iv4xrAgentState;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.IntVec2D;
import eu.iv4xr.framework.spatial.Vec3;
import nethack.agent.navigation.NavUtils;
import nethack.object.EntityType;
import nethack.object.Level;
import nethack.utils.NethackSurface_NavGraph;
import nethack.utils.NethackSurface_NavGraph.Door;
import nethack.utils.NethackSurface_NavGraph.Tile;
import nethack.utils.NethackSurface_NavGraph.Wall;
import nl.uu.cs.aplib.mainConcepts.Environment;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.List;
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
    static final Logger logger = LogManager.getLogger(AgentState.class);
    public LayeredAreasNavigation<Tile, NethackSurface_NavGraph> multiLayerNav;

    @Override
    public AgentEnv env() {
        return (AgentEnv) super.env();
    }

    /**
     * We are not going to keep an Nav-graph, but will instead keep a
     * layered-nav-graphs.
     */
    @Override
    public Navigatable<Void> worldNavigation() {
        throw new UnsupportedOperationException();
    }

    /**
     * We are not going to keep an Nav-graph, but will instead keep a
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
        addNewNavGraph(false);
        return this;
    }

    public WorldEntity auxState() {
        return worldmodel().elements.get("aux");
    }

    private void addNewNavGraph(boolean withPortal) {
        NethackSurface_NavGraph newNav = new NethackSurface_NavGraph();
        newNav.sizeX = Level.WIDTH;
        newNav.sizeY = Level.HEIGHT;
        newNav.diagonalMovementPossible = true;

        if (withPortal) {
            Vec3 playerPosition = env().app.gameState.player.position;
            Tile lowPortal = new Tile(playerPosition.x, playerPosition.y);
            Tile highPortal = new Tile(playerPosition.x, playerPosition.y);
            multiLayerNav.addNextArea(newNav, lowPortal, highPortal, true);
        } else {
            multiLayerNav.addNextArea(newNav, null, null, false);
        }
    }

    @Override
    public void updateState(String agentId) {
        super.updateState(agentId);
        WorldEntity aux = auxState();
        Serializable[] seenTiles = (Serializable[]) aux.properties.get("visibleTiles");
        for (Serializable entry_ : seenTiles) {
            Serializable[] entry = (Serializable[]) entry_;
            int levelNumber = (int) entry[0];
            IntVec2D pos = (IntVec2D) entry[1];
            EntityType type = (EntityType) entry[2];

            // If detecting a new maze, need to allocate a nav-graph for this maze:
            if (levelNumber >= multiLayerNav.areas.size()) {
                logger.info("Adding a new level: " + levelNumber);
                addNewNavGraph(true);
            }

            multiLayerNav.markAsSeen(new Pair<>(levelNumber, new Tile(pos.x, pos.y)));
            switch (type) {
                case VOID:
                case WALL:
                case BOULDER:
                    multiLayerNav.addObstacle(new Pair<>(levelNumber, new Wall(pos.x, pos.y)));
                    break;
                case CORRIDOR:
                case FLOOR:
                    multiLayerNav.removeObstacle(new Pair<>(levelNumber, new Tile(pos.x, pos.y)));
                    break;
                case DOOR:
                    boolean isOpen = !env().app.gameState.level().getEntity(pos).closedDoor();
                    multiLayerNav.addObstacle(new Pair<>(levelNumber, new Door(pos.x, pos.y, isOpen)));
                    break;
                default:
                    // Current logic does not treat other objects as obstacles (Player/Pet/Monster)
                    multiLayerNav.setBlockingState(new Pair<>(levelNumber, new Tile(pos.x, pos.y)), false);
                    break;
            }
        }

        // removing entities that are no longer in the game-board, except players:
        Serializable[] removedEntities = (Serializable[]) aux.properties.get("recentlyRemoved");
        for (Serializable entry_ : removedEntities) {
            String id = (String) entry_;
            if (id == null) {
                continue;
            }

            if (id.equals("player")) {
                continue;
            }
            this.worldmodel.elements.remove(id);
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
        Tile p = NavUtils.toTile(env().app.gameState.player.position);

        List<WorldEntity> ms = worldmodel.elements
                .values().stream().filter(e -> e.type == type.name()
                        && NavUtils.levelId(player) == NavUtils.levelId(e) && NavUtils.adjacent(p, NavUtils.toTile(e.position), allowDiagonally))
                .collect(Collectors.toList());

        if (ms.size() > 0) {
            logger.debug(String.format("Found %d %s nearby (diagonal=%b)", ms.size(), type.name(), allowDiagonally));
        }

        return ms;
    }

    public boolean nextToEntity(EntityType entityType, boolean allowDiagonally) {
        return adjacentEntities(entityType, allowDiagonally).size() > 0;
    }

    public boolean nextToEntity(String entityId, boolean allowDiagonally) {
        Tile p = NavUtils.toTile(env().app.gameState.player.position);
        List<WorldEntity> ms = worldmodel.elements.values().stream().filter(e -> e.position != null && e.id == entityId && NavUtils.adjacent(p, NavUtils.toTile(e.position), allowDiagonally)).collect(Collectors.toList());
        return ms.size() > 0;
    }
}
