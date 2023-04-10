package nethack.world.tiles;

import agent.navigation.surface.Tile;
import agent.navigation.surface.Walkable;
import util.CustomVec3D;

public class Water extends Tile implements Walkable, Viewable {
  boolean isVisible;

  public Water(CustomVec3D pos) {
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

  public Tile updatedTile(Tile newTile) {
    if (!(newTile instanceof Water)) {
      return newTile;
    }
    setSeen(getSeen() || newTile.getSeen());
    setVisibility(((Water) newTile).getVisibility());
    return this;
  }

  public boolean equals(Object o) {
    if (!(o instanceof Water)) {
      return false;
    }

    Water water = (Water) o;
    return loc.equals(water.loc) && getVisibility() == water.getVisibility();
  }
}
