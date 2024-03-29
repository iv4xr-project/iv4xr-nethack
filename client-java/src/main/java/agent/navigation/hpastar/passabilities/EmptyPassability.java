//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar.passabilities;

import agent.navigation.hpastar.ConcreteMap;
import agent.navigation.hpastar.IPassability;
import agent.navigation.hpastar.NavType;
import agent.navigation.hpastar.Size;
import agent.navigation.hpastar.infrastructure.Constants;
import agent.navigation.hpastar.utils.RefSupport;
import java.util.Arrays;
import util.ColoredStringBuilder;
import util.CustomVec2D;

public class EmptyPassability implements IPassability {
  final Size size;
  final boolean[][] obstacles;
  final boolean[][] canMoveDiagonally;

  public EmptyPassability(Size size) {
    this.size = size;

    // Init everything as obstacle
    obstacles = new boolean[size.height][size.width];
    for (int y = 0; y < size.height; y++) {
      Arrays.fill(obstacles[y], true);
    }

    canMoveDiagonally = new boolean[size.height][size.width];
  }

  @Override
  public void updateCanMoveDiagonally(CustomVec2D pos, boolean canMoveDiagonally) {
    boolean updated = this.canMoveDiagonally[pos.y][pos.x] != canMoveDiagonally;
    this.canMoveDiagonally[pos.y][pos.x] = canMoveDiagonally;
  }

  @Override
  public void updateObstacle(CustomVec2D pos, boolean isObstacle) {
    boolean updated = this.obstacles[pos.y][pos.x] != isObstacle;
    this.obstacles[pos.y][pos.x] = isObstacle;
  }

  public boolean cannotEnter(CustomVec2D pos, RefSupport<Integer> cost) {
    cost.setValue(Constants.COST_ONE);
    return obstacles[pos.y][pos.x];
  }

  public boolean cannotEnter(CustomVec2D pos) {
    return obstacles[pos.y][pos.x];
  }

  @Override
  public boolean canMoveDiagonal(CustomVec2D pos1, CustomVec2D pos2) {
    return canMoveDiagonally[pos1.y][pos1.x] && canMoveDiagonally[pos2.y][pos2.x];
  }

  @Override
  public boolean canMoveDiagonal(CustomVec2D pos) {
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
    return new ConcreteMap(NavType.OctileUnicost, size, slice);
  }

  @Override
  public ConcreteMap getConcreteMap() {
    return new ConcreteMap(NavType.OctileUnicost, size, this);
  }

  public String toString() {
    ColoredStringBuilder csb = new ColoredStringBuilder();
    for (int y = 0; y < size.height; y++) {
      for (int x = 0; x < size.width; x++) {
        if (cannotEnter(new CustomVec2D(x, y))) {
          csb.append('#');
        } else {
          csb.append('.');
        }
      }
      if (y != size.height - 1) {
        csb.newLine();
      }
    }

    return csb.toString();
  }
}
