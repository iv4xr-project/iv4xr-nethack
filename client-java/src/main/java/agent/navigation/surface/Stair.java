package agent.navigation.surface;

import eu.iv4xr.framework.spatial.IntVec2D;

public class Stair extends Tile {
  public boolean goesUp = false;

  public Stair(IntVec2D pos, boolean goesUp) {
    super(pos);
    this.goesUp = goesUp;
  }

  public Stair(int x, int y, boolean goesUp) {
    super(x, y);
    this.goesUp = goesUp;
  }

  public char toChar() {
    return goesUp ? '<' : '>';
  }
}
