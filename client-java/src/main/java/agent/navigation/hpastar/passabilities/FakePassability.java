//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp.Passabilities;

import CS2JNet.JavaSupport.language.RefSupport;
import HPASharp.Infrastructure.Constants;
import HPASharp.IPassability;
import HPASharp.Position;

public class FakePassability   implements IPassability
{
    float obstaclePercentage = 0.20f;
    private boolean[][] obstacles = new boolean[][]();
    public FakePassability(int width, int height) throws Exception {
        obstacles = new boolean[width, height];
        createObstacles(obstaclePercentage,width,height,true);
    }

    private Random random = new Random(1000);
    public boolean canEnter(Position pos, RefSupport<int> cost) throws Exception {
        cost.setValue(Constants.COST_ONE);
        return !obstacles[pos.Y, pos.X];
    }

    /**
    * Creates obstacles in the map
    */
    private void createObstacles(float obstaclePercentage, int width, int height, boolean avoidDiag) throws Exception {
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ RAND_MAX = 0x7fff;
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ numberNodes = width * height;
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ numberObstacles = (int)(obstaclePercentage * numberNodes);
        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ count = 0;count < numberObstacles;)
        {
            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ nodeId = random.Next() / (RAND_MAX / numberNodes + 1) % (width * height);
            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ x = nodeId % width;
            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ y = nodeId / width;
            if (!obstacles[x, y])
            {
                if (avoidDiag)
                {
                    if (!ConflictDiag(y, x, -1, -1, width, height) && !ConflictDiag(y, x, -1, +1, width, height) && !ConflictDiag(y, x, +1, -1, width, height) && !ConflictDiag(y, x, +1, +1, width, height))
                    {
                        obstacles[x, y] = true;
                        ++count;
                    }

                }
                else
                {
                    obstacles[x, y] = true;
                    ++count;
                }
            }

        }
    }

    public Position getRandomFreePosition() throws Exception {
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ x = random.Next(40);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ y = random.Next(40);
        while (obstacles[x, y])
        {
            x = random.Next(40);
            y = random.Next(40);
        }
        return new Position(x, y);
    }

    private boolean conflictDiag(int row, int col, int roff, int coff, int width, int height) throws Exception {
        // Avoid generating cofigurations like:
        //
        //    @   or   @
        //     @      @
        //
        // that favor one grid topology over another.
        if ((row + roff < 0) || (row + roff >= height) || (col + coff < 0) || (col + coff >= width))
            return false;

        if (obstacles[col + coff, row + roff])
        {
            if (!obstacles[col + coff, row] && !obstacles[col, row + roff])
                return true;

        }

        return false;
    }

}
