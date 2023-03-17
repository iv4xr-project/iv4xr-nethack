package agent.navigation.surface;

import util.CustomVec3D;

public class Wall extends Tile implements Printable {
  private boolean isVisible = false;
  public int timesSearched = 0;

  public Wall(CustomVec3D pos) {
    super(pos);
  }

  @Override
  public char toChar() {
    return seen ? 'W' : 'w';
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
