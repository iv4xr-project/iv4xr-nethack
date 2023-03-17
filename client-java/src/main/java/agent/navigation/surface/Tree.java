package agent.navigation.surface;

import util.CustomVec3D;

public class Tree extends Tile implements Printable {
  private boolean isVisible;
  public final boolean seeThrough = true;

  public Tree(CustomVec3D pos) {
    super(pos);
  }

  public char toChar() {
    return '#';
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
}
