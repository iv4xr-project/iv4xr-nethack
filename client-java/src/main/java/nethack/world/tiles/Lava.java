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
  public boolean isVisible() {
    return isVisible;
  }

  @Override
  public void setVisible(boolean isVisible) {
    this.isVisible = isVisible;
  }

  public boolean equals(Object o) {
    if (!(o instanceof Lava)) {
      return false;
    }

    return loc.equals(((Lava) o).loc);
  }
}
