package agent.navigation.surface;

import java.util.Objects;
import util.CustomVec2D;
import util.CustomVec3D;

public abstract class Tile {
  public CustomVec3D loc;
  public final CustomVec2D pos;
  public boolean seen = false;

  public Tile(CustomVec3D loc) {
    this.loc = loc;
    this.pos = loc.pos;
  }

  public abstract char toChar();

  public abstract Tile updatedTile(Tile newTile);

  public void setSeen(boolean seen) {
    this.seen = seen;
  }

  public boolean getSeen() {
    return seen;
  }

  public void markAsSeen() {
    setSeen(true);
  }

  public void resetSeen() {
    setSeen(false);
  }

  public abstract boolean equals(Object o);

  @Override
  public int hashCode() {
    return Objects.hash(loc);
  }

  @Override
  public String toString() {
    return loc.toString();
  }
}
