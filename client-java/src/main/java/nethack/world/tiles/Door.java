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

  public boolean isSecret = false;
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
    Door door = ((Door) newTile);
    seen = door.getSeen() || seen;
    setIsSecret(door.getIsSecret());
    return this;
  }

  public char toChar() {
    return isOpen ? 'O' : 'X';
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
        && locked == door.locked
        && trapped == door.trapped
        && isSecret == door.isSecret;
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

  @Override
  public boolean getIsSecret() {
    return isSecret;
  }

  @Override
  public void setIsSecret(boolean isSecret) {
    this.isSecret = isSecret;
  }
}
