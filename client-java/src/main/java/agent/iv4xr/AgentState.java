package agent.iv4xr;

import agent.navigation.HierarchicalNavigation;
import eu.iv4xr.framework.extensions.pathfinding.Navigatable;
import eu.iv4xr.framework.mainConcepts.Iv4xrAgentState;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import java.util.*;
import nethack.NetHack;
import nethack.object.Entity;
import nethack.object.Player;
import nethack.object.Turn;
import nethack.world.Level;
import nethack.world.Surface;
import nl.uu.cs.aplib.mainConcepts.Environment;
import nl.uu.cs.aplib.utils.Pair;
import util.ColoredStringBuilder;
import util.CustomVec3D;
import util.Loggers;

/**
 * Provides an implementation of agent state {@link nl.uu.cs.aplib.mainConcepts.SimpleState}. The
 * state defined here inherits from {@link Iv4xrAgentState}, so it also keeps a historical
 * WorldModel.
 *
 * @author wish
 */
public class AgentState extends Iv4xrAgentState<Void, Player, Entity> {
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

  public List<Entity> getWorldEntities() {
    return worldmodel.getCurrentElements();
  }

  public HierarchicalNavigation hierarchicalNav() {
    return app().gameState.dungeon.hierarchicalNav;
  }

  public CustomVec3D loc() {
    return worldmodel.player.current.location;
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
    // Remove all entities that are in vision range but can't be seen.
    List<String> idsToRemove = new ArrayList<>();
    Loggers.WOMLogger.info("WOM contains %d elements", worldmodel.elements.size());
    for (Entity we : getWorldEntities()) {
      if (loc().lvl != we.loc.lvl) {
        continue;
      }

      // Item is gone from worldEntities
      if (env().app.gameState.getLevel().entities.stream()
          .noneMatch(entity -> entity.getId().equals(we.getId()))) {
        idsToRemove.add(we.getId());
      }
    }

    // Separate loop since it changes the map
    for (String id : idsToRemove) {
      worldmodel.removeElement(id);
    }
  }

  public Pair<AgentState, WorldModel<Player, Entity>> getNewWOM() {
    WorldModel<Player, Entity> wom = env().observe(Player.ID);
    worldmodel.mergeNewObservation(wom);
    return new Pair<>(this, worldmodel);
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
        String.format("%%-%ds  %%-%ds%n", Level.SIZE.width * 2 + 40, Level.SIZE.width),
        game[n + 2],
        hierarchicalMap[n + 2]);
    csb.appendf(tripleFormatString, "", "", hierarchicalMap[n + 3]);
    System.out.print(csb);
  }
}
