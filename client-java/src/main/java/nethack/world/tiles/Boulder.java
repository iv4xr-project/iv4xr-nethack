package nethack.world.tiles;

import agent.navigation.surface.Tile;
import util.CustomVec3D;

public class Boulder extends Tile implements Viewable {
  private boolean isVisible;

  public Boulder(CustomVec3D pos) {
    super(pos);
  }

  @Override
  public char toChar() {
    return '`';
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
    if (!(newTile instanceof Boulder)) {
      return newTile;
    }
    setSeen(getSeen() || newTile.getSeen());
    setVisibility(((Boulder) newTile).getVisibility());
    return this;
  }

  public boolean equals(Object o) {
    if (!(o instanceof Boulder)) {
      return false;
    }

    Boulder other = (Boulder) o;
    return loc.equals(other.loc) && getVisibility() == other.getVisibility();
  }
}
