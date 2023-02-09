//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar.passabilities;

import agent.navigation.hpastar.ConcreteMap;
import agent.navigation.hpastar.IPassability;
import agent.navigation.hpastar.Size;
import agent.navigation.hpastar.infrastructure.Constants;
import agent.navigation.hpastar.utils.RefSupport;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.Random;

public class FakePassability implements IPassability {
  final boolean[][] obstacles;
  private final Random random = new Random(0);

  public FakePassability(Size size) {
    obstacles = new boolean[size.width][size.height];
    noObstaclesInCluster();
    //    createRandomObstacles(true);
  }

  public boolean canEnter(IntVec2D pos, RefSupport<Integer> cost) {
    cost.setValue(Constants.COST_ONE);
    return !obstacles[pos.x][pos.y];
  }

  @Override
  public boolean canMoveDiagonal(IntVec2D pos1, IntVec2D pos2) {
    return true;
  }

  @Override
  public ConcreteMap getConcreteMap() {
    return null;
  }

  private void noObstaclesInCluster() {
    for (int x = 0; x < obstacles.length; x++) {
      for (int y = 0; y < obstacles[0].length; y++) {
        if (y >= 1 && x >= 1 && y <= 6 && x <= 6) {
          obstacles[x][y] = false;
        } else if (x == 4 && (y == 7 || y == 8 || y == 9)) {
          // 'Door'
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
    int width = obstacles.length;
    int height = obstacles[0].length;
    int numberNodes = obstacles.length * obstacles[0].length;
    int numberObstacles = (int) (obstaclePercentage * numberNodes);
    for (int count = 0; count < numberObstacles; count++) {
      int randInt = Math.abs(random.nextInt());
      int nodeId = randInt / (RAND_MAX / numberNodes + 1) % numberNodes;
      int x = nodeId % width;
      int y = nodeId / width;
      if (!obstacles[x][y]) {
        if (avoidDiag) {
          if (!conflictDiag(y, x, -1, -1, width, height)
              && !conflictDiag(y, x, -1, +1, width, height)
              && !conflictDiag(y, x, +1, -1, width, height)
              && !conflictDiag(y, x, +1, +1, width, height)) {
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
    int x = random.nextInt(obstacles.length);
    int y = random.nextInt(obstacles[0].length);
    while (obstacles[x][y]) {
      x = random.nextInt(obstacles.length);
      y = random.nextInt(obstacles[0].length);
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
      return false;
    }

    if (obstacles[col + coff][row + roff]) {
      if (!obstacles[col + coff][row] && !obstacles[col][row + roff]) {
        return true;
      }
    }

    return false;
  }
}
