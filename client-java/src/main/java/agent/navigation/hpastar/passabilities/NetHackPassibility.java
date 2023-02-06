package agent.navigation.hpastar.passabilities;

import agent.navigation.NetHackSurface;
import agent.navigation.hpastar.IPassability;
import agent.navigation.hpastar.infrastructure.Constants;
import agent.navigation.hpastar.utils.RefSupport;
import agent.navigation.surface.StraightWalkable;
import agent.navigation.surface.Tile;
import agent.navigation.surface.Walkable;
import eu.iv4xr.framework.spatial.IntVec2D;
import nethack.object.Level;

public class NetHackPassibility implements IPassability {
  NetHackSurface surface;

  public NetHackPassibility(NetHackSurface surface) {
    this.surface = surface;
  }

  @Override
  public boolean canEnter(IntVec2D pos, RefSupport<Integer> movementCost) {
    movementCost.setValue(Constants.COST_ONE);
    Tile t = surface.getTile(pos);
    if (t == null) {
      return false;
    }
    //    return true;
    return t instanceof StraightWalkable || t.getClass() == Tile.class;
  }

  @Override
  public boolean canMoveDiagonal(IntVec2D pos1, IntVec2D pos2) {
    Tile a = surface.getTile(pos1);
    Tile b = surface.getTile(pos2);
    return a instanceof Walkable && b instanceof Walkable;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int y = 0; y < Level.HEIGHT; y++) {
      for (int x = 0; x < Level.WIDTH; x++) {
        if (canEnter(new IntVec2D(x, y), new RefSupport<>())) {
          sb.append(' ');
        } else {
          sb.append('▓');
        }
      }
      sb.append(System.lineSeparator());
    }

    return sb.toString();
  }
}
