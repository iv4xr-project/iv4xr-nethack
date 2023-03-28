package nethack.world.tiles;

import agent.navigation.surface.Tile;
import util.CustomVec3D;

public class Tree extends Tile implements Viewable {
  private boolean isVisible;
  public final boolean seeThrough = true;

  public Tree(CustomVec3D pos) {
    super(pos);
  }

  public char toChar() {
    return '#';
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
    if (!(o instanceof Tree)) {
      return false;
    }

    return loc.equals(((Tree) o).loc);
  }
}
