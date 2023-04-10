package nethack.world.tiles;

import agent.navigation.surface.Tile;
import util.CustomVec3D;

public class IronBars extends Tile implements Viewable {
  private boolean isVisible;
  public final boolean seeThrough = true;

  public IronBars(CustomVec3D pos) {
    super(pos);
  }

  public char toChar() {
    return '#';
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
    if (!(newTile instanceof IronBars)) {
      return newTile;
    }
    setSeen(getSeen() || newTile.getSeen());
    setVisibility(((IronBars) newTile).getVisibility());
    return this;
  }

  public boolean equals(Object o) {
    if (!(o instanceof IronBars)) {
      return false;
    }

    IronBars ironBars = (IronBars) o;
    return loc.equals(ironBars.loc) && getVisibility() == ironBars.getVisibility();
  }
}
