package nethack.agent;

import eu.iv4xr.framework.mainConcepts.Iv4xrEnvironment;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.IntVec2D;
import eu.iv4xr.framework.spatial.Vec3;
import nethack.NetHack;
import nethack.object.Command;
import nethack.object.Entity;
import nethack.object.EntityType;
import nethack.object.Player;

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
    public NetHack app;

    Set<EntityType> unimportantTypes = new HashSet<>(
            Arrays.asList(EntityType.WALL, EntityType.CORRIDOR, EntityType.FLOOR, EntityType.VOID, EntityType.PLAYER)
    );

    public AgentEnv(NetHack app) {
        this.app = app;
    }

    /**
     * Observing does not advance the game turn.
     */
    @Override
    public WorldModel observe(String agentId) {
        WorldModel wom = new WorldModel();

        wom.agentId = agentId;
        wom.position = app.gameState.player.position;
        wom.timestamp = app.gameState.stats.time;

        WorldEntity aux = mkGameAuxState();
        wom.elements.put(aux.id, aux);
        wom.elements.put(app.gameState.player.id, toWorldEntity(app.gameState.player));

        // adding visible objects:
        List<IntVec2D> visibleTiles = app.level().visibleTiles(app.gameState.player.position2D);
        for (IntVec2D pos : visibleTiles) {
            Entity e = app.level().getEntity(pos);
            if (unimportantTypes.contains(e.type)) {
                continue;
            }
            e.assignId(pos.x, pos.y);
            wom.elements.put(e.id, toWorldEntity(e, pos.x, pos.y));
        }

        // time-stamp the elements:
        for (WorldEntity e : wom.elements.values()) {
            e.timestamp = wom.timestamp;
        }
        return wom;
    }

    public WorldModel action(Command action) {
        app.step(action);
        return observe("player");
    }

    WorldEntity toWorldEntity(Player p) {
        WorldEntity we = new WorldEntity("id", p.id, true);
        we.properties.put("level", app.gameState.stats.zeroIndexLevelNumber);
        we.properties.put("hp", app.gameState.player.hp);
        we.properties.put("hpmax", app.gameState.player.hpMax);
        we.position = p.position;
        return we;
    }

    WorldEntity toWorldEntity(Entity e, int x, int y) {
        if (e.type == EntityType.VOID) {
            return null;
        }

        WorldEntity we;
        int level = app.gameState.stats.zeroIndexLevelNumber;
        String type = e.type.name();

        switch (e.type) {
            case WALL:
            case FLOOR:
            case CORRIDOR:
                we = new WorldEntity(e.id, type, false);
                we.properties.put("level", level);
                break;
            case DOOR:
                we = new WorldEntity(e.id, type, false);
                we.properties.put("level", level);
                we.properties.put("closed", e.closedDoor());
                break;
            default:
                we = new WorldEntity(e.id, type, true);
                we.properties.put("level", level);
                break;
        }

        we.position = new Vec3(x, y, level);
        return we;
    }

    WorldEntity mkGameAuxState() {
        WorldEntity aux = new WorldEntity("aux", "aux", true);
        aux.properties.put("time", app.gameState.stats.time);
        aux.properties.put("status", app.gameState.done);

        // recently removed objects:
        String[] removed = new String[app.gameState.level().removedEntities.size()];
        int k = 0;
        for (Entity e : app.gameState.level().removedEntities) {
            removed[k] = e.id;
            k++;
        }
        aux.properties.put("recentlyRemoved", removed);

        // currently visible tiles:
        List<IntVec2D> visibleTiles_ = app.level().visibleTiles(app.gameState.player.position2D);
        Serializable[] visibleTiles = new Serializable[visibleTiles_.size()];
        k = 0;
        for (IntVec2D pos : visibleTiles_) {
            int levelId = app.gameState.stats.zeroIndexLevelNumber;
            Entity e = app.level().getEntity(pos);
            EntityType entityType = e.type;
            Serializable[] entry = {levelId, pos, entityType};
            visibleTiles[k] = entry;
            k++;
        }
        aux.properties.put("visibleTiles", visibleTiles);

        return aux;
    }
}
