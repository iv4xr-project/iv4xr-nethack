package agent;

import eu.iv4xr.framework.mainConcepts.Iv4xrEnvironment;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.IntVec2D;
import eu.iv4xr.framework.spatial.Vec3;
import nethack.NetHack;
import nethack.object.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation of {@link nl.uu.cs.aplib.mainConcepts.Environment}
 * to connect iv4xr/aplib agents to the game NetHack.
 *
 * @author wish
 */
public class AgentEnv extends Iv4xrEnvironment {
    static final Logger logger = LogManager.getLogger(AgentLoggers.WOMLogger);
    public NetHack app;

    private static final Set<EntityType> ignoredTypes = new HashSet<>(
            Arrays.asList(EntityType.WALL, EntityType.CORRIDOR, EntityType.FLOOR, EntityType.ICE, EntityType.VOID, EntityType.DOORWAY)
    );

    public AgentEnv(NetHack app) {
        this.app = app;
    }

    // Observing does not advance the game turn.
    @Override
    public WorldModel observe(String agentId) {
        WorldModel wom = new WorldModel();

        wom.agentId = agentId;
        wom.position = app.gameState.player.position;
        wom.timestamp = app.gameState.stats.time;

        WorldEntity aux = mkGameAuxState();
        wom.elements.put(aux.id, aux);
        wom.elements.put(Player.ID, toWorldEntity(app.gameState.player));

        // Add changed coordinates
        Level level = app.level();
        for (IntVec2D pos : level.changedCoordinates) {
            Entity e = level.getEntity(pos);

            // Unimportant types, and player is updated separately
            if (ignoredTypes.contains(e.type) || e.type == EntityType.PLAYER) {
                continue;
            }

            e.assignId(pos);
            logger.debug(String.format("%s %s Added", e.id, pos));
            wom.elements.put(e.id, toWorldEntity(e, pos));
        }

        // Time-stamp the elements:
        for (WorldEntity e : wom.elements.values()) {
            e.timestamp = wom.timestamp;
        }

        return wom;
    }

    public WorldModel action(Command action) {
        app.step(action);
        return observe(Player.ID);
    }

    WorldEntity toWorldEntity(Player p) {
        WorldEntity we = new WorldEntity(Player.ID, EntityType.PLAYER.name(), true);
        we.properties.put("level", app.gameState.stats.zeroIndexLevelNumber);
        we.properties.put("hp", app.gameState.player.hp);
        we.properties.put("hpmax", app.gameState.player.hpMax);
        we.position = p.position;
        return we;
    }

    WorldEntity toWorldEntity(Entity e, IntVec2D pos) {
        WorldEntity we;
        int level = app.gameState.stats.zeroIndexLevelNumber;
        String type = e.type.name();

        if (e.type == EntityType.DOOR) {
            we = new WorldEntity(e.id, type, false);
            we.properties.put("level", level);
            we.properties.put("closed", e.closedDoor());
        } else {
            we = new WorldEntity(e.id, type, true);
            we.properties.put("level", level);
        }

        we.position = new Vec3(pos.x, pos.y, level);
        return we;
    }

    WorldEntity mkGameAuxState() {
        WorldEntity aux = new WorldEntity("aux", "aux", true);
        aux.properties.put("time", app.gameState.stats.time);
        aux.properties.put("status", app.gameState.done);
        aux.properties.put("levelId", app.gameState.stats.zeroIndexLevelNumber);

        // Part of the map that has updates that might be relevant to the map navigation state
        Level level = app.level();
        List<IntVec2D> changedCoordinates_ = level.changedCoordinates;
        Serializable[] changedCoordinates = new Serializable[changedCoordinates_.size()];
        int k = 0;
        for (IntVec2D pos : changedCoordinates_) {
            Entity e = level.getEntity(pos);
            EntityType entityType = e.type;
            Serializable[] entry = {pos, entityType};
            changedCoordinates[k++] = entry;
        }
        aux.properties.put("changedCoordinates", changedCoordinates);

        return aux;
    }
}
