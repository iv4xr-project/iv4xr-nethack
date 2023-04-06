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

  public boolean equals(Object o) {
    if (!(o instanceof IronBars)) {
      return false;
    }

    return loc.equals(((IronBars) o).loc);
  }
}