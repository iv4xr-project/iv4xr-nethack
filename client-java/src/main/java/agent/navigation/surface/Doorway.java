package agent.navigation.surface;

import util.CustomVec3D;

public class Doorway extends Tile implements Walkable, Printable {
  boolean isVisible;

  public Doorway(CustomVec3D pos) {
    super(pos);
  }

  @Override
  public char toChar() {
    return '.';
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
}
