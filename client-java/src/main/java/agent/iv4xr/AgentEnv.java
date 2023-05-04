package agent.iv4xr;

import eu.iv4xr.framework.mainConcepts.Iv4xrEnvironment;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import nethack.NetHack;
import nethack.object.Entity;
import nethack.object.Player;
import nethack.world.Level;
import util.Loggers;

/**
 * Provides an implementation of {@link nl.uu.cs.aplib.mainConcepts.Environment} to connect
 * iv4xr/aplib agents to the game NetHack.
 *
 * @author wish
 */
public class AgentEnv extends Iv4xrEnvironment<Player, Entity> {
  public final NetHack app;

  public AgentEnv(NetHack app) {
    this.app = app;
  }

  // Observing does not advance the game turn.
  @Override
  public WorldModel<Player, Entity> observe(String agentId) {
    WorldModel<Player, Entity> wom = new WorldModel<>();
    wom.player = wom.new PlayerRecord(app.gameState.player);
    wom.timestamp = app.gameState.stats.turn.turnNr;

    // Add changed coordinates
    Level level = app.level();
    for (Entity entity : level.entities) {
      String id = entity.getId();
      Loggers.WOMLogger.debug("%s %s Added", id, entity.pos);
      wom.putElement(id, wom.new WorldEntityRecord(entity));
    }

    // Time-stamp the elements:
    for (Entity e : wom.getCurrentElements()) {
      e.timestamp = wom.timestamp;
    }

    return wom;
  }
}
