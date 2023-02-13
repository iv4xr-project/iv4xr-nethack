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
import java.util.Random;

public class FakePassability implements IPassability {
  private final Size size;
  private final boolean[][] obstacles;
  private final Random random = new Random(0);

  public FakePassability(Size size, boolean randomPreset) {
    this.size = size;
    obstacles = new boolean[size.width][size.height];

    if (randomPreset) {
      createRandomObstacles(true);
    } else {
      noObstaclesInCluster();
    }
  }

  private FakePassability(Size size) {
    this.size = size;
    obstacles = new boolean[size.width][size.height];
  }

  @Override
  public void updateCanMoveDiagonally(IntVec2D pos, boolean canMoveDiagonally) {}

  @Override
  public void updateObstacle(IntVec2D pos, boolean isObstacle) {
    boolean update = obstacles[pos.x][pos.y] != isObstacle;
    obstacles[pos.x][pos.y] = isObstacle;
  }

  public boolean cannotEnter(IntVec2D pos, RefSupport<Integer> cost) {
    cost.setValue(Constants.COST_ONE);
    return obstacles[pos.x][pos.y];
  }

  @Override
  public boolean canMoveDiagonal(IntVec2D pos1, IntVec2D pos2) {
    return true;
  }

  @Override
  public boolean canMoveDiagonal(IntVec2D pos) {
    return true;
  }

  @Override
  public ConcreteMap slice(int horizOrigin, int vertOrigin, Size size) {
    FakePassability slice = new FakePassability(size);
    for (int x = horizOrigin, relX = 0; x < this.size.width && relX < size.width; x++, relX++) {
      for (int y = vertOrigin, relY = 0; y < this.size.height && relY < size.height; y++, relY++) {
        slice.obstacles[relX][relY] = obstacles[x][y];
      }
    }
    return new ConcreteMap(TileType.OctileUnicost, size, slice);
  }

  @Override
  public ConcreteMap getConcreteMap() {
    return null;
  }

  private void noObstaclesInCluster() {
    for (int x = 0; x < size.width; x++) {
      for (int y = 0; y < size.height; y++) {
        if (y >= 1 && x >= 1 && y <= 9 && x <= 6) {
          obstacles[x][y] = false;
        } else {
          obstacles[x][y] = true;
        }
      }
    }
  }

  /** Creates obstacles in the map */
  private void createRandomObstacles(boolean avoidDiag) {
    float obstaclePercentage = 0.20f;
    int RAND_MAX = Integer.MAX_VALUE;
    int numberNodes = size.width * size.height;
    int numberObstacles = (int) (obstaclePercentage * numberNodes);
    for (int count = 0; count < numberObstacles; count++) {
      int randInt = Math.abs(random.nextInt());
      int nodeId = randInt / (RAND_MAX / numberNodes + 1) % numberNodes;
      int x = nodeId % size.width;
      int y = nodeId / size.width;
      if (!obstacles[x][y]) {
        if (avoidDiag) {
          if (conflictDiag(y, x, -1, -1, size.width, size.height)
              && conflictDiag(y, x, -1, +1, size.width, size.height)
              && conflictDiag(y, x, +1, -1, size.width, size.height)
              && conflictDiag(y, x, +1, +1, size.width, size.height)) {
            obstacles[x][y] = true;
            ++count;
          }
        } else {
          obstacles[x][y] = true;
          ++count;
        }
      }
    }
  }

  public IntVec2D getRandomFreePosition() {
    int x = random.nextInt(size.width);
    int y = random.nextInt(size.height);
    while (obstacles[x][y]) {
      x = random.nextInt(size.width);
      y = random.nextInt(size.height);
    }
    return new IntVec2D(x, y);
  }

  private boolean conflictDiag(int row, int col, int roff, int coff, int width, int height) {
    // Avoid generating configurations like:
    //
    //    @   or   @
    //     @      @
    //
    // that favor one grid topology over another.
    if ((row + roff < 0) || (row + roff >= height) || (col + coff < 0) || (col + coff >= width)) {
      return true;
    }

    if (obstacles[col + coff][row + roff]) {
      return obstacles[col + coff][row] || obstacles[col][row + roff];
    }

    return true;
  }
}
