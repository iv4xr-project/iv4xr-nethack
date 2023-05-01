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
    return !isSecret();
  }

  @Override
  public WalkableType getWalkableType() {
    return WalkableType.Diagonal;
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
    if (!(newTile instanceof Corridor)) {
      return newTile;
    }
    setSeen(getSeen() || newTile.getSeen());
    setIsSecret(((Corridor) newTile).getIsSecret());
    setVisibility(((Corridor) newTile).getVisibility());
    return this;
  }

  public boolean equals(Object o) {
    if (!(o instanceof Corridor corridor)) {
      return false;
    }

    return loc.equals(corridor.loc)
        && isSecret == corridor.isSecret
        && getVisibility() == corridor.getVisibility();
  }
}
