package nethack.world.tiles;

import agent.navigation.surface.Climbable;
import agent.navigation.surface.Tile;
import agent.navigation.surface.Walkable;
import util.CustomVec3D;

public class Stair extends Tile implements Climbable, Walkable, Viewable {
  private boolean isVisible = false;
  public final ClimbType climbType;

  public Stair(CustomVec3D pos, boolean goesUp) {
    super(pos);
    this.climbType = goesUp ? ClimbType.Up : ClimbType.Down;
  }

  @Override
  public char toChar() {
    return climbType == ClimbType.Up ? '<' : '>';
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
  public WalkableType getWalkableType() {
    return WalkableType.Diagonal;
  }

  @Override
  public ClimbType getClimbType() {
    return climbType;
  }

  public boolean equals(Object o) {
    if (!(o instanceof Stair)) {
      return false;
    }

    return loc.equals(((Stair) o).loc);
  }
}
