package nethack.world.tiles;

import agent.navigation.surface.Tile;
import agent.navigation.surface.Walkable;
import util.CustomVec3D;

public class Moat extends Tile implements Walkable, Viewable {
  boolean isVisible;

  public Moat(CustomVec3D pos) {
    super(pos);
  }

  public char toChar() {
    return '~';
  }

  @Override
  public boolean isSeeThrough() {
    return true;
  }

  @Override
  public boolean getVisibility() {
    return isVisible;
  }

  @Override
  public void setVisibility(boolean isVisible) {
    if (isVisible) {
      markAsSeen();
    }
    this.isVisible = isVisible;
  }

  @Override
  public boolean isWalkable() {
    return true;
  }

  @Override
  public WalkableType getWalkableType() {
    return WalkableType.Diagonal;
  }

  public boolean equals(Object o) {
    if (!(o instanceof Moat)) {
      return false;
    }

    return loc.equals(((Moat) o).loc);
  }
}
