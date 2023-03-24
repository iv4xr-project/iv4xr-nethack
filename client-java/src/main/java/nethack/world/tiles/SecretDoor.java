package nethack.world.tiles;

import agent.navigation.surface.Tile;
import agent.navigation.surface.Walkable;
import util.CustomVec3D;

public class SecretDoor extends Tile implements Walkable, Viewable {
  public boolean broken;
  public boolean isOpen;
  public boolean closed;
  public boolean locked;
  public boolean trapped;
  public boolean seeThrough = isOpen || broken;
  public boolean hasDoor = !isOpen && !closed;
  boolean isVisible = false;

  public SecretDoor(
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

  @Override
  public Tile updatedTile(Tile newTile) {
    if (this.getClass() != newTile.getClass()) {
      return newTile;
    }
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
    return isOpen;
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

  @Override
  public WalkableType getWalkableType() {
    return hasDoor ? WalkableType.Straight : WalkableType.Diagonal;
  }
}
