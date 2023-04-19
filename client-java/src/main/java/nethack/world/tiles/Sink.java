package nethack.world.tiles;

import agent.navigation.surface.Tile;
import agent.navigation.surface.Walkable;
import util.CustomVec3D;

public class Sink extends Tile implements Walkable, Viewable {
  private boolean isVisible;

  public Sink(CustomVec3D pos) {
    super(pos);
  }

  public char toChar() {
    return seen ? 'S' : 's';
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
    return true;
  }

  @Override
  public WalkableType getWalkableType() {
    return WalkableType.Diagonal;
  }

  public Tile updatedTile(Tile newTile) {
    if (!(newTile instanceof Sink)) {
      return newTile;
    }
    setSeen(getSeen() || newTile.getSeen());
    setVisibility(((Sink) newTile).getVisibility());
    return this;
  }

  public boolean equals(Object o) {
    if (!(o instanceof Sink)) {
      return false;
    }

    Sink sink = (Sink) o;
    return loc.equals(sink.loc) && getVisibility() == sink.getVisibility();
  }
}
