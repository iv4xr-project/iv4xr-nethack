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
    return 'W';
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
    if (this.getClass() != newTile.getClass()) {
      return newTile;
    }
    setSeen(getSeen() || newTile.getSeen());
    setVisibility(((Wall) newTile).getVisibility());
    return this;
  }

  public boolean equals(Object o) {
    if (!(o instanceof Wall)) {
      return false;
    }

    Wall wall = (Wall) o;
    return loc.equals(wall.loc) && getVisibility() == wall.getVisibility();
  }
}
