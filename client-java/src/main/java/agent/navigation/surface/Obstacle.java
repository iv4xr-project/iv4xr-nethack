package agent.navigation.surface;

import eu.iv4xr.framework.spatial.IntVec2D;

public abstract class Obstacle extends Tile {
  public final boolean seeThrough = false;

  public Obstacle(IntVec2D pos) {
    super(pos);
  }
}
