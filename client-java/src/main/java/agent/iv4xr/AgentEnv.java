package agent.iv4xr;
;
import eu.iv4xr.framework.mainConcepts.Iv4xrEnvironment;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nethack.NetHack;
import nethack.enums.SymbolType;
import nethack.object.Command;
import nethack.object.Player;
import nethack.object.Symbol;
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
  // Ignores all dungeon features
  private static final Set<SymbolType> ignoredTypes =
      new HashSet<>(
          Arrays.asList(
              SymbolType.WALL,
              SymbolType.DOOR,
              SymbolType.CORRIDOR,
              SymbolType.FLOOR,
              SymbolType.ICE,
              SymbolType.VOID,
              SymbolType.DOORWAY,
              SymbolType.STAIRS_UP,
              SymbolType.STAIRS_DOWN,
              SymbolType.SINK,
              SymbolType.TREE,
              SymbolType.LAVA,
              SymbolType.WATER,
              SymbolType.THRONE,
              SymbolType.FOUNTAIN));
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
    for (CustomVec2D pos : level.changedMapCoordinates) {
      Symbol e = level.getSymbol(pos);
      // Unimportant types, and player is updated separately
      if (e == null || ignoredTypes.contains(e.type) || e.type == SymbolType.PLAYER) {
        continue;
      }

      String id = e.createId(pos);
      int levelNr = app.gameState.getLevelNr();
      Loggers.WOMLogger.debug("%s %s Added", id, pos);
      wom.elements.put(id, toWorldEntity(e, new CustomVec3D(levelNr, pos)));
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

  WorldEntity toWorldEntity(Symbol e, CustomVec3D loc) {
    String id = e.createId(loc.pos);
    WorldEntity we = new WorldEntity(id, e.type.name(), true);
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
    for (CustomVec2D pos : changedCoordinates_) {
      Symbol e = level.getSymbol(pos);
      if (e == null) {
        continue;
      }

      SymbolType symbolType = e.type;
      Serializable[] entry = {pos, symbolType};
      changedCoordinates[k++] = entry;
    }
    aux.properties.put("changedCoordinates", changedCoordinates);

    return aux;
  }
}
