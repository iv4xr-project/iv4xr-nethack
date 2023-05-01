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

  public Tile updatedTile(Tile newTile) {
    if (!(newTile instanceof Floor floor)) {
      return newTile;
    }

    isShop = floor.isShop || isShop;
    setSeen(getSeen() || newTile.getSeen());
    setVisibility(floor.getVisibility());
    return this;
  }

  public boolean equals(Object o) {
    if (!(o instanceof Floor f)) {
      return false;
    }

    return loc.equals(f.loc) && getVisibility() == f.getVisibility();
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
