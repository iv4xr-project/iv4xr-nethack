package nethack.world.tiles;

import agent.navigation.surface.Tile;
import agent.navigation.surface.Walkable;
import util.CustomVec3D;

public class Corridor extends Tile implements Walkable, Viewable, Secret {
  boolean isSecret;
  boolean isVisible;

  public Corridor(CustomVec3D pos) {
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

  public Corridor updatedTile(Corridor newTile) {
    if (getClass() != newTile.getClass()) {
      return newTile;
    }
    seen = ((Corridor) newTile).getSeen() || seen;
    setIsSecret(((Corridor) newTile).getIsSecret());
    return this;
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
    if (!(o instanceof Corridor)) {
      return false;
    }

    Corridor corridor = (Corridor) o;
    return loc.equals(corridor.loc) && isSecret == corridor.isSecret;
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
