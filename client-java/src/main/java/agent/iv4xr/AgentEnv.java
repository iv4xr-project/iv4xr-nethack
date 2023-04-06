package agent.iv4xr;
;
import eu.iv4xr.framework.mainConcepts.Iv4xrEnvironment;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import java.io.Serializable;
import java.util.List;
import nethack.NetHack;
import nethack.enums.SymbolType;
import nethack.object.Command;
import nethack.object.Entity;
import nethack.object.Player;
import nethack.world.Level;
import util.CustomVec2D;
import util.CustomVec3D;
import util.Loggers;

/**
 * Provides an implementation of {@link nl.uu.cs.aplib.mainConcepts.Environment} to connect
 * iv4xr/aplib agents to the game NetHack.
 *
 * @author wish
 */
public class AgentEnv extends Iv4xrEnvironment {
  public final NetHack app;

  public AgentEnv(NetHack app) {
    this.app = app;
  }

  // Observing does not advance the game turn.
  @Override
  public WorldModel observe(String agentId) {
    WorldModel wom = new WorldModel();

    wom.agentId = agentId;
    wom.position = app.gameState.player.location.toVec3();
    wom.timestamp = app.gameState.stats.turn.time;

    WorldEntity aux = mkGameAuxState();
    wom.elements.put(aux.id, aux);
    wom.elements.put(Player.ID, toWorldEntity(app.gameState.player));

    // Add changed coordinates
    Level level = app.level();
    for (Entity entity : level.entities) {
      int levelNr = app.gameState.getLevelNr();
      String id = entity.toId();
      Loggers.WOMLogger.debug("%s %s Added", id, entity.pos);
      wom.elements.put(id, toWorldEntity(entity, new CustomVec3D(levelNr, entity.pos)));
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
    WorldEntity we = new WorldEntity(Player.ID, SymbolType.PLAYER.name(), true);
    we.properties.put("hp", app.gameState.player.hp);
    we.properties.put("hpmax", app.gameState.player.hpMax);
    we.position = p.location.toVec3();
    return we;
  }

  WorldEntity toWorldEntity(Entity e, CustomVec3D loc) {
    WorldEntity we = new WorldEntity(e.toId(), e.entityClass.name(), true);
    we.position = loc.toVec3();
    return we;
  }

  WorldEntity mkGameAuxState() {
    WorldEntity aux = new WorldEntity("aux", "aux", true);
    aux.properties.put("time", app.gameState.stats.turn.time);
    aux.properties.put("status", app.gameState.done);
    aux.properties.put("levelNr", app.gameState.getLevelNr());

    // Part of the map that has updates that might be relevant to the map navigation state
    Level level = app.level();
    List<CustomVec2D> changedCoordinates_ = level.changedMapCoordinates;
    Serializable[] changedCoordinates = new Serializable[changedCoordinates_.size()];
    int k = 0;
    //    for (CustomVec2D pos : changedCoordinates_) {
    //      Symbol e = level.getSymbol(pos);
    //      if (e == null) {
    //        continue;
    //      }
    //
    //      Serializable[] entry = {pos, symbolType};
    //      changedCoordinates[k++] = entry;
    //    }
    aux.properties.put("changedCoordinates", changedCoordinates);

    return aux;
  }
}
