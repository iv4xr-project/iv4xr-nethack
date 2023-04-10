package nethack.world.tiles;

import agent.navigation.surface.Tile;
import util.CustomVec3D;

public class Lava extends Tile implements Viewable {
  boolean isVisible;

  public Lava(CustomVec3D pos) {
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

  public Tile updatedTile(Tile newTile) {
    if (!(newTile instanceof Lava)) {
      return newTile;
    }
    setSeen(getSeen() || newTile.getSeen());
    setVisibility(((Lava) newTile).getVisibility());
    return this;
  }

  public boolean equals(Object o) {
    if (!(o instanceof Lava)) {
      return false;
    }

    Lava lava = (Lava) o;
    return loc.equals(lava.loc) && getVisibility() == lava.getVisibility();
  }
}
