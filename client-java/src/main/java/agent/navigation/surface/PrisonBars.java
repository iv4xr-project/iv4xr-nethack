package agent.navigation.surface;

import util.CustomVec3D;

public class PrisonBars extends Tile implements Printable {
  private boolean isVisible;
  public final boolean seeThrough = true;

  public PrisonBars(CustomVec3D pos) {
    super(pos);
  }

  public char toChar() {
    return '#';
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
