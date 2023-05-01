package nethack.world.tiles;

import agent.navigation.surface.Tile;
import agent.navigation.surface.Walkable;
import util.CustomVec3D;

public class Door extends Tile implements Walkable, Viewable, Secret {
  public boolean broken;
  public boolean isOpen;
  public boolean closed;
  public boolean locked;
  public boolean trapped;
  public boolean isShopDoor;
  private int flags;

  public boolean isSecret = false;
  boolean isVisible = false;

  public Door(
      CustomVec3D pos,
      boolean broken,
      boolean isOpen,
      boolean closed,
      boolean locked,
      boolean trapped,
      int flags) {
    super(pos);
    this.broken = broken;
    this.isOpen = isOpen;
    this.closed = closed;
    this.locked = locked;
    this.trapped = trapped;
    this.flags = flags;
  }

  public char toChar() {
    return isWalkable() ? 'O' : 'X';
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
    if (isSecret()) {
      return false;
    }
    if (broken) {
      return true;
    }
    if (!isOpen && !closed && !locked && !trapped) {
      return true;
    }
    return isOpen;
  }

  @Override
  public WalkableType getWalkableType() {
    return !isOpen || !closed ? WalkableType.Straight : WalkableType.Diagonal;
  }

  @Override
  public boolean getIsSecret() {
    return isSecret;
  }

  @Override
  public void setIsSecret(boolean isSecret) {
    this.isSecret = isSecret;
  }

  public Tile updatedTile(Tile newTile) {
    if (!(newTile instanceof Door door)) {
      return newTile;
    }

    setSeen(door.getSeen() || getSeen());
    setIsSecret(door.getIsSecret());
    setVisibility(door.isVisible());
    broken = door.broken;
    isOpen = door.isOpen;
    closed = door.closed;
    locked = door.locked;
    trapped = door.trapped;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Door door)) {
      return false;
    }

    return loc.equals(door.loc)
        && getVisibility() == door.getVisibility()
        && broken == door.broken
        && isOpen == door.isOpen
        && closed == door.closed
        && locked == door.locked
        && trapped == door.trapped
        && isSecret == door.isSecret;
  }
}
