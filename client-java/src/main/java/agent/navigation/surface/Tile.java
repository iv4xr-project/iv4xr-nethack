package agent.navigation.surface;

import java.util.Objects;
import util.CustomVec2D;
import util.CustomVec3D;

public class Tile {
  public CustomVec3D loc;
  public final CustomVec2D pos;
  public boolean seen = false;

  public Tile(CustomVec3D loc) {
    this.loc = loc;
    this.pos = loc.pos;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Tile)) {
      return false;
    }

    Tile t = (Tile) o;
    return loc.equals(t.loc);
  }

  @Override
  public int hashCode() {
    return Objects.hash(loc);
  }

  @Override
  public String toString() {
    return loc.toString();
  }
}
