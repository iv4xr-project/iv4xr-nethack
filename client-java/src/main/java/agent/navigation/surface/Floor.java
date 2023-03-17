package agent.navigation.surface;

import util.CustomVec3D;

public class Floor extends Tile implements Walkable, Printable {
  private boolean isVisible;

  public Floor(CustomVec3D pos) {
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
