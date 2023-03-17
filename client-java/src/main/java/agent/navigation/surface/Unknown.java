package agent.navigation.surface;

import util.CustomVec3D;

public class Unknown extends Tile implements Printable, Walkable {
  private boolean isVisible;

  public Unknown(CustomVec3D pos) {
    super(pos);
  }

  @Override
  public char toChar() {
    return '?';
  }

  @Override
  public boolean isWalkable() {
    return true;
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
}
