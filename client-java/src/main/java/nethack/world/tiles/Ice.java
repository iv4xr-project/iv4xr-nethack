package nethack.world.tiles;

import agent.navigation.surface.Tile;
import agent.navigation.surface.Walkable;
import util.CustomVec3D;

public class Ice extends Tile implements Walkable, Viewable {
  private boolean isVisible;

  public Ice(CustomVec3D pos) {
    super(pos);
  }

  @Override
  public char toChar() {
    return '·';
  }

  @Override
  public boolean isSeeThrough() {
    return true;
  }

  @Override
  public boolean getVisibility() {
    if (isVisible) {
      markAsSeen();
    }
    return isVisible;
  }

  @Override
  public void setVisibility(boolean isVisible) {
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
    if (!(o instanceof Ice)) {
      return false;
    }

    return loc.equals(((Ice) o).loc);
  }
}
