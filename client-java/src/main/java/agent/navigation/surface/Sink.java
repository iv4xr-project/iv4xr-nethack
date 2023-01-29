package agent.navigation.surface;

import eu.iv4xr.framework.spatial.IntVec2D;

public class Sink extends Tile implements Walkable, Printable {
  private boolean isVisible;

  public Sink(IntVec2D pos) {
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
}
