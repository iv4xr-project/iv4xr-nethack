package agent.navigation.surface;

import eu.iv4xr.framework.spatial.IntVec2D;

public class Lava extends Tile implements Printable {
  boolean isVisible;

  public Lava(IntVec2D pos) {
    super(pos);
  }

  public char toChar() {
    return '~';
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
