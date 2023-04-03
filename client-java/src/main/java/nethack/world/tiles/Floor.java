package nethack.world.tiles;

import agent.navigation.surface.Tile;
import agent.navigation.surface.Walkable;
import util.CustomVec3D;

public class Floor extends Tile implements Walkable, Viewable, Shop {
  private boolean isVisible;
  public boolean isShop;

  public Floor(CustomVec3D pos) {
    super(pos);
  }

  @Override
  public char toChar() {
    return '·';
  }

  @Override
  public boolean isSeeThrough() {
    return true;
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

  @Override
  public boolean isShop() {
    return isShop;
  }

  public boolean equals(Object o) {
    if (!(o instanceof Floor)) {
      return false;
    }

    Floor f = (Floor) o;
    return loc.equals(f.loc);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  public Floor updatedTile(Floor newTile) {
    if (getClass() != newTile.getClass()) {
      return newTile;
    }
    seen = newTile.getSeen() || seen;
    isShop = newTile.isShop || isShop;
    resetVisibility();
    return this;
  }
}
