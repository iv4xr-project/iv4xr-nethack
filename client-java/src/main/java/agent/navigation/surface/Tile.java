package agent.navigation.surface;

import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.Objects;

public class Tile {
  public IntVec2D pos;
  public boolean seen = false;

  public Tile(IntVec2D pos) {
    this.pos = pos;
  }

  public Tile(int x, int y) {
    this.pos = new IntVec2D(x, y);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }

    if (o instanceof Tile) {
      Tile t = (Tile) o;
      return pos.x == t.pos.x && pos.y == t.pos.y;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(pos);
  }

  @Override
  public String toString() {
    return "⁇";
  }
}
