package agent.iv4xr;
;
import eu.iv4xr.framework.mainConcepts.Iv4xrEnvironment;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.IntVec2D;
import eu.iv4xr.framework.spatial.Vec3;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nethack.NetHack;
import nethack.enums.EntityType;
import nethack.object.Command;
import nethack.object.Entity;
import nethack.object.Level;
import nethack.object.Player;
import nl.uu.cs.aplib.utils.Pair;
import util.Loggers;

/**
 * Provides an implementation of {@link nl.uu.cs.aplib.mainConcepts.Environment} to connect
 * iv4xr/aplib agents to the game NetHack.
 *
 * @author wish
 */
public class AgentEnv extends Iv4xrEnvironment {
  private static final Set<EntityType> ignoredTypes =
      new HashSet<>(
          Arrays.asList(
              EntityType.WALL,
              EntityType.DOOR,
              EntityType.CORRIDOR,
              EntityType.FLOOR,
              EntityType.ICE,
              EntityType.VOID,
              EntityType.DOORWAY,
              EntityType.STAIRS_UP,
              EntityType.STAIRS_DOWN,
              EntityType.SINK));
  public final NetHack app;

  public AgentEnv(NetHack app) {
    this.app = app;
  }

  // Observing does not advance the game turn.
  @Override
  public WorldModel observe(String agentId) {
    WorldModel wom = new WorldModel();

    wom.agentId = agentId;
    wom.position = app.gameState.player.position;
    wom.timestamp = app.gameState.stats.turn.time;

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

      String id = e.createId(pos);
      int levelNr = app.gameState.getLevelNr();
      Loggers.WOMLogger.debug("%s %s Added", id, pos);
      wom.elements.put(id, toWorldEntity(e, new Pair<>(levelNr, pos)));
    }

    // Time-stamp the elements:
    for (WorldEntity e : wom.elements.values()) {
      e.timestamp = wom.timestamp;
    }

    return wom;
  }

  public WorldModel commands(Command... commands) {
    app.step(commands);
    return observe(Player.ID);
  }

  WorldEntity toWorldEntity(Player p) {
    WorldEntity we = new WorldEntity(Player.ID, EntityType.PLAYER.name(), true);
    we.properties.put("hp", app.gameState.player.hp);
    we.properties.put("hpmax", app.gameState.player.hpMax);
    we.position = p.position;
    return we;
  }

  WorldEntity toWorldEntity(Entity e, Pair<Integer, IntVec2D> pos) {
    String id = e.createId(pos.snd);
    WorldEntity we = new WorldEntity(id, e.type.name(), true);
    we.position = new Vec3(pos.snd.x, pos.snd.y, pos.fst);
    return we;
  }

  WorldEntity mkGameAuxState() {
    WorldEntity aux = new WorldEntity("aux", "aux", true);
    aux.properties.put("time", app.gameState.stats.turn.time);
    aux.properties.put("status", app.gameState.done);
    aux.properties.put("levelNr", app.gameState.getLevelNr());

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
