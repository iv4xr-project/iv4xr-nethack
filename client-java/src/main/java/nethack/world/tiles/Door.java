package nethack.world.tiles;

import agent.navigation.surface.Tile;
import agent.navigation.surface.Walkable;
import util.CustomVec3D;

public class Door extends Tile implements Walkable, Viewable {
  public boolean broken;
  public boolean isOpen;
  public boolean closed;
  public boolean locked;
  public boolean trapped;
  boolean isVisible = false;

  public Door(
      CustomVec3D pos,
      boolean broken,
      boolean isOpen,
      boolean closed,
      boolean locked,
      boolean trapped) {
    super(pos);
    this.broken = broken;
    this.isOpen = isOpen;
    this.closed = closed;
    this.locked = locked;
    this.trapped = trapped;
  }

  public Tile updatedTile(Tile newTile) {
    if (getClass() != newTile.getClass()) {
      return newTile;
    }
    newTile.setSeen(newTile.getSeen() || getSeen());
    return this;
  }

  public char toChar() {
    if (isOpen) {
      return seen ? 'O' : 'o';
    } else {
      return seen ? 'X' : 'x';
    }
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Door)) {
      return false;
    }

    Door door = (Door) o;
    return loc.equals(door.loc)
        && broken == door.broken
        && isOpen == door.isOpen
        && closed == door.closed
        && locked
        && door.locked
        && trapped == door.trapped;
  }

  @Override
  public boolean isSeeThrough() {
    return isOpen || broken;
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
    return isSeeThrough();
  }

  @Override
  public WalkableType getWalkableType() {
    return !isOpen || !closed ? WalkableType.Straight : WalkableType.Diagonal;
  }
}
