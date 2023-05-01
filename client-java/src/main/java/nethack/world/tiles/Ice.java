package nethack.world.tiles;

import agent.navigation.surface.Tile;
import agent.navigation.surface.Walkable;
import util.CustomVec3D;

public class Ice extends Tile implements Walkable, Viewable {
  private boolean isVisible;

  public Ice(CustomVec3D pos) {
    super(pos);
  }

  @Override
  public char toChar() {
    return '·';
  }

  @Override
  public boolean getVisibility() {
    if (isVisible) {
      markAsSeen();
    }
    return isVisible;
  }

  @Override
  public void setVisibility(boolean isVisible) {
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

  public Tile updatedTile(Tile newTile) {
    if (!(newTile instanceof Ice)) {
      return newTile;
    }
    setSeen(getSeen() || newTile.getSeen());
    setVisibility(((Ice) newTile).getVisibility());
    return this;
  }

  public boolean equals(Object o) {
    if (!(o instanceof Ice ice)) {
      return false;
    }

    return loc.equals(ice.loc) && getVisibility() == ice.getVisibility();
  }
}
