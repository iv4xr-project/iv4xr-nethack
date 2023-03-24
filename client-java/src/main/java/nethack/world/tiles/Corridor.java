package nethack.world.tiles;

import agent.navigation.surface.Tile;
import agent.navigation.surface.Walkable;
import util.CustomVec3D;

public class Corridor extends Tile implements Walkable, Viewable {
  boolean isVisible;

  public Corridor(CustomVec3D pos) {
    super(pos);
  }

  public char toChar() {
    return '-';
  }

  @Override
  public boolean isSeeThrough() {
    return false;
  }

  @Override
  public boolean isVisible() {
    return isVisible;
  }

  @Override
  public void setVisible(boolean isVisible) {
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
    if (!(o instanceof Corridor)) {
      return false;
    }

    return loc.equals(((Corridor) o).loc);
  }
}
