package agent.navigation.surface;

import eu.iv4xr.framework.spatial.IntVec2D;

public class Door extends Tile implements StraightWalkable, Printable {
  public boolean isOpen = false;
  public boolean isLocked = false;

  public boolean seeThrough = isOpen;

  boolean isVisible = false;

  public Door(IntVec2D pos) {
    super(pos);
  }

  public Door(IntVec2D pos, boolean isOpen) {
    super(pos);
    this.isOpen = isOpen;
  }

  public void setBlockingState(boolean isBlocking) {
    if (isBlocking) {
      isOpen = false;
    } else {
      isOpen = true;
      isLocked = false;
    }
  }

  public char toChar() {
    if (isOpen) {
      return seen ? 'O' : 'o';
    } else {
      return seen ? 'X' : 'x';
    }
  }

  @Override
  public boolean isSeeThrough() {
    return isOpen && !isLocked;
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
    return isSeeThrough();
  }
}
