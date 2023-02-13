//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar.passabilities;

import agent.navigation.hpastar.ConcreteMap;
import agent.navigation.hpastar.IPassability;
import agent.navigation.hpastar.Size;
import agent.navigation.hpastar.TileType;
import agent.navigation.hpastar.infrastructure.Constants;
import agent.navigation.hpastar.utils.RefSupport;
import eu.iv4xr.framework.spatial.IntVec2D;

public class EmptyPassability implements IPassability {
  final Size size;
  final boolean[][] obstacles;
  final boolean[][] canMoveDiagonally;

  public EmptyPassability(Size size) {
    this.size = size;

    // Init everything as obstacle
    obstacles = new boolean[size.height][size.width];
    for (int y = 0; y < size.height; y++) {
      for (int x = 0; x < size.width; x++) {
        obstacles[y][x] = true;
      }
    }

    canMoveDiagonally = new boolean[size.height][size.width];
  }

  @Override
  public void updateCanMoveDiagonally(IntVec2D pos, boolean canMoveDiagonally) {
    boolean updated = this.canMoveDiagonally[pos.y][pos.x] != canMoveDiagonally;
    this.canMoveDiagonally[pos.y][pos.x] = canMoveDiagonally;
  }

  @Override
  public void updateObstacle(IntVec2D pos, boolean isObstacle) {
    boolean updated = this.obstacles[pos.y][pos.x] != isObstacle;
    this.obstacles[pos.y][pos.x] = isObstacle;
  }

  public boolean cannotEnter(IntVec2D pos, RefSupport<Integer> cost) {
    cost.setValue(Constants.COST_ONE);
    return obstacles[pos.y][pos.x];
  }

  @Override
  public boolean canMoveDiagonal(IntVec2D pos1, IntVec2D pos2) {
    return canMoveDiagonally[pos1.y][pos1.x] && canMoveDiagonally[pos2.y][pos2.x];
  }

  @Override
  public boolean canMoveDiagonal(IntVec2D pos) {
    return canMoveDiagonally[pos.y][pos.x];
  }

  @Override
  public ConcreteMap slice(int horizOrigin, int vertOrigin, Size size) {
    EmptyPassability slice = new EmptyPassability(size);
    for (int x = horizOrigin, relX = 0; x < this.size.width && relX < size.width; x++, relX++) {
      for (int y = vertOrigin, relY = 0; y < this.size.height && relY < size.height; y++, relY++) {
        slice.obstacles[relY][relX] = obstacles[y][x];
        slice.canMoveDiagonally[relY][relX] = canMoveDiagonally[y][x];
      }
    }
    return new ConcreteMap(TileType.OctileUnicost, size, slice);
  }

  @Override
  public ConcreteMap getConcreteMap() {
    return new ConcreteMap(TileType.OctileUnicost, size, this);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int y = 0; y < size.height; y++) {
      for (int x = 0; x < size.width; x++) {
        if (cannotEnter(new IntVec2D(x, y), new RefSupport<>())) {
          sb.append('#');
        } else {
          sb.append('.');
        }
      }
      if (y != size.height - 1) {
        sb.append(System.lineSeparator());
      }
    }

    return sb.toString();
  }
}
