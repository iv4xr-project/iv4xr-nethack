//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp;


// implements edges in the abstract graph
public class AbsTilingEdgeInfo
{
    private int __Cost = new int();
    public int getCost() {
        return __Cost;
    }

    public void setCost(int value) {
        __Cost = value;
    }

    private int __Level = new int();
    public int getLevel() {
        return __Level;
    }

    public void setLevel(int value) {
        __Level = value;
    }

    private boolean __IsInterEdge = new boolean();
    public boolean getIsInterEdge() {
        return __IsInterEdge;
    }

    public void setIsInterEdge(boolean value) {
        __IsInterEdge = value;
    }

    public AbsTilingEdgeInfo(int cost, int level, boolean inter) throws Exception {
        setCost(cost);
        setLevel(level);
        setIsInterEdge(inter);
    }

    public String toString() {
        try
        {
            return ("cost: " + getCost() + "; level: " + getLevel() + "; inter: " + getIsInterEdge());
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

    public void printInfo() throws Exception {
        Console.WriteLine(this.toString());
    }

}
