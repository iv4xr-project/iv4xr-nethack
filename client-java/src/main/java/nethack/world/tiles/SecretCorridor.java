package nethack.world.tiles;

import agent.navigation.surface.Tile;
import agent.navigation.surface.Walkable;
import util.CustomVec3D;

public class SecretCorridor extends Tile implements Walkable, Viewable {
  boolean isVisible = false;

  public SecretCorridor(CustomVec3D pos) {
    super(pos);
  }

  public char toChar() {
    return '-';
  }

  @Override
  public boolean isSeeThrough() {
    return false;
  }

  @Override
  public boolean getVisibility() {
    return isVisible;
  }

  @Override
  public void setVisibility(boolean isVisible) {
    if (isVisible) {
      markAsSeen();
    }
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

  public boolean equals(Object o) {
    if (!(o instanceof SecretCorridor)) {
      return false;
    }

    return loc.equals(((SecretCorridor) o).loc);
  }
}
