package agent.navigation.hpastar.passabilities;

import agent.navigation.hpastar.ConcreteMap;
import agent.navigation.hpastar.IPassability;
import agent.navigation.hpastar.Size;
import agent.navigation.hpastar.TileType;
import agent.navigation.hpastar.infrastructure.Constants;
import agent.navigation.hpastar.utils.RefSupport;
import eu.iv4xr.framework.spatial.IntVec2D;

public class NetHackPassability implements IPassability {
  private final Size size;
  private final boolean[][] obstacles;
  private final boolean[][] canMoveDiagonally;

  public NetHackPassability(Size size) {
    this.size = size;
    obstacles = new boolean[size.height][size.width];
    canMoveDiagonally = new boolean[size.height][size.width];
  }

  // Boolean indicates whether it is updated
  public boolean updateCanMoveDiagonally(IntVec2D pos, boolean canMoveDiagonally) {
    boolean updated = this.canMoveDiagonally[pos.y][pos.x] != canMoveDiagonally;
    this.canMoveDiagonally[pos.y][pos.x] = canMoveDiagonally;
    return updated;
  }

  // Boolean indicates whether it is updated
  public boolean updateObstacle(IntVec2D pos, boolean isObstacle) {
    boolean updated = this.obstacles[pos.y][pos.x] != isObstacle;
    this.obstacles[pos.y][pos.x] = isObstacle;
    return updated;
  }

  @Override
  public boolean canEnter(IntVec2D pos, RefSupport<Integer> movementCost) {
    movementCost.setValue(Constants.COST_ONE);
    return obstacles[pos.y][pos.x]; // t instanceof StraightWalkable || t.getClass() == Tile.class;
  }

  @Override
  public boolean canMoveDiagonal(IntVec2D pos1, IntVec2D pos2) {
    return canMoveDiagonally[pos1.y][pos1.x] && canMoveDiagonally[pos2.y][pos2.x];
  }

  @Override
  public ConcreteMap slice(int horizOrigin, int vertOrigin, Size size) {
    NetHackPassability slice = new NetHackPassability(size);
    for (int x = horizOrigin, relX = 0; x < this.size.width && relX < size.width; x++, relX++) {
      for (int y = vertOrigin, relY = 0; y < this.size.height && relY < size.height; y++, relY++) {
        slice.obstacles[relY][relX] = obstacles[y][x];
        slice.canMoveDiagonally[relY][relX] = canMoveDiagonally[y][x];
      }
    }
    return new ConcreteMap(TileType.OctileUnicost, size, slice);
  }

  public ConcreteMap getConcreteMap() {
    return new ConcreteMap(TileType.OctileUnicost, size, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int y = 0; y < size.height; y++) {
      for (int x = 0; x < size.width; x++) {
        if (!canEnter(new IntVec2D(x, y), new RefSupport<>())) {
          sb.append('▓');
        } else if (canMoveDiagonally[y][x]) {
          sb.append('*');
        } else {
          sb.append('+');
        }
      }
      sb.append(System.lineSeparator());
    }

    return sb.toString();
  }
}
