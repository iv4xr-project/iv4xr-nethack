package agent.navigation.surface;

import util.CustomVec3D;

public class Corridor extends Tile implements Walkable, Printable {
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
}
