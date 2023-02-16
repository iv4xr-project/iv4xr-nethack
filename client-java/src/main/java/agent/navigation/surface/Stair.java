package agent.navigation.surface;

import eu.iv4xr.framework.spatial.IntVec2D;

public class Stair extends Tile implements Climbable, Walkable, Printable {
  private boolean isVisible = false;
  public final ClimbType climbType;

  public Stair(IntVec2D pos, ClimbType climbType) {
    super(pos);
    this.climbType = climbType;
  }

  public Stair(int x, int y, ClimbType climbType) {
    super(x, y);
    this.climbType = climbType;
  }

  @Override
  public char toChar() {
    return climbType == ClimbType.Ascendable ? '<' : '>';
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

  @Override
  public ClimbType getClimbType() {
    return climbType;
  }

  @Override
  public Climbable getOtherSide() {
    return null;
  }
}
