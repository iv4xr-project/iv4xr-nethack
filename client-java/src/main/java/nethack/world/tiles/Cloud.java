package nethack.world.tiles;

import agent.navigation.surface.Tile;
import agent.navigation.surface.Walkable;
import util.CustomVec3D;

public class Cloud extends Tile implements Walkable, Viewable {
  private boolean isVisible;

  public Cloud(CustomVec3D pos) {
    super(pos);
  }

  @Override
  public char toChar() {
    return '#';
  }

  @Override
  public boolean isSeeThrough() {
    return false;
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
    if (!(o instanceof Cloud)) {
      return false;
    }

    return loc.equals(((Cloud) o).loc);
  }
}
