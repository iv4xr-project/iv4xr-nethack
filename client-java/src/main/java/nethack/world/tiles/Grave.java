package nethack.world.tiles;

import agent.navigation.surface.Tile;
import agent.navigation.surface.Walkable;
import util.CustomVec3D;

public class Grave extends Tile implements Walkable, Viewable {
  boolean isVisible;

  public Grave(CustomVec3D pos) {
    super(pos);
  }

  public char toChar() {
    return '|';
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
    if (!(newTile instanceof Grave)) {
      return newTile;
    }
    setSeen(getSeen() || newTile.getSeen());
    setVisibility(((Grave) newTile).getVisibility());
    return this;
  }

  public boolean equals(Object o) {
    if (!(o instanceof Grave)) {
      return false;
    }

    Grave grave = (Grave) o;
    return loc.equals(grave.loc) && getVisibility() == grave.getVisibility();
  }
}
