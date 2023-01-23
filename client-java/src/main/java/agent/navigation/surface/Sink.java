package agent.navigation.surface;

import eu.iv4xr.framework.spatial.IntVec2D;

public class Sink extends Tile {
  public Sink(IntVec2D pos) {
    super(pos);
  }

  @Override
  public char toChar() {
    return 's';
  }
}
