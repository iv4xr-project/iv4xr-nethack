package agent.navigation.surface;

import eu.iv4xr.framework.spatial.IntVec2D;

public class PrisonBars extends Obstacle {
  public final boolean seeThrough = true;

  public PrisonBars(IntVec2D pos) {
    super(pos);
  }

  @Override
  public char toChar() {
    return seen ? 'P' : 'p';
  }
}
