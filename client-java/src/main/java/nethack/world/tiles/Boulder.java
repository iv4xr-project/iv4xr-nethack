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
    if (!(o instanceof Boulder)) {
      return false;
    }

    return loc.equals(((Boulder) o).loc);
  }
}
