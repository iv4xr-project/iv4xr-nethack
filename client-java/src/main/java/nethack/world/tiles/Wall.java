package nethack.world.tiles;

import agent.navigation.surface.Tile;
import util.CustomVec3D;

public class Wall extends Tile implements Viewable {
  private boolean isVisible = false;
  public int timesSearched = 0;

  public Wall(CustomVec3D pos) {
    super(pos);
  }

  @Override
  public char toChar() {
    return seen ? 'W' : 'w';
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

  public boolean equals(Object o) {
    if (!(o instanceof Wall)) {
      return false;
    }

    return loc.equals(((Wall) o).loc);
  }
}
