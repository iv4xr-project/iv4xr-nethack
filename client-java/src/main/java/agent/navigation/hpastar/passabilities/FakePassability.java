//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar.passabilities;

import agent.navigation.hpastar.IPassability;
import agent.navigation.hpastar.infrastructure.Constants;
import agent.navigation.hpastar.utils.RefSupport;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.Random;

public class FakePassability implements IPassability {
  float obstaclePercentage = 0.20f;
  private final boolean[][] obstacles;

  public FakePassability(int width, int height) {
    obstacles = new boolean[width][height];
    createObstacles(obstaclePercentage, width, height, true);
  }

  private Random random = new Random(1000);

  public boolean canEnter(IntVec2D pos, RefSupport<Integer> cost) {
    cost.setValue(Constants.COST_ONE);
    return !obstacles[pos.y][pos.x];
  }

  /** Creates obstacles in the map */
  private void createObstacles(float obstaclePercentage, int width, int height, boolean avoidDiag) {
    int RAND_MAX = 0x7fff;
    int numberNodes = width * height;
    int numberObstacles = (int) (obstaclePercentage * numberNodes);
    for (int count = 0; count < numberObstacles; ) {
      int nodeId = random.nextInt() / (RAND_MAX / numberNodes + 1) % (width * height);
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
    int x = random.nextInt(40);
    int y = random.nextInt(40);
    while (obstacles[x][y]) {
      x = random.nextInt(40);
      y = random.nextInt(40);
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
