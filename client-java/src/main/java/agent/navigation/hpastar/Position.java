//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp;


public class Position
{
    public Position() {
    }

    public Position(int x, int y) throws Exception {
        X = x;
        Y = y;
    }

    public String toString() {
        try
        {
            return "(" + X + "," + Y + ")";
        }
        catch (RuntimeException __dummyCatchVar0)
        {
            throw __dummyCatchVar0;
        }
        catch (Exception __dummyCatchVar0)
        {
            throw new RuntimeException(__dummyCatchVar0);
        }

    }

    public int X = new int();
    public int Y = new int();
}
