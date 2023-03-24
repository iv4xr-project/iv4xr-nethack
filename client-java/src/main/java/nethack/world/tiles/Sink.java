package nethack.world.tiles;

import agent.navigation.surface.Tile;
import agent.navigation.surface.Walkable;
import util.CustomVec3D;

public class Sink extends Tile implements Walkable, Viewable {
  private boolean isVisible;

  public Sink(CustomVec3D pos) {
    super(pos);
  }

  public char toChar() {
    return 's';
  }

  @Override
  public boolean isSeeThrough() {
    return true;
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
    if (!(o instanceof Sink)) {
      return false;
    }

    return loc.equals(((Sink) o).loc);
  }
}
