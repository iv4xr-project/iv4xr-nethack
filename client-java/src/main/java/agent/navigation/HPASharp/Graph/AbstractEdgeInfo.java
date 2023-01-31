//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp.Graph;

import HPASharp.Graph.AbstractNode;
import HPASharp.Infrastructure.Id;

public class AbstractEdgeInfo
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

    private boolean __IsInterClusterEdge = new boolean();
    public boolean getIsInterClusterEdge() {
        return __IsInterClusterEdge;
    }

    public void setIsInterClusterEdge(boolean value) {
        __IsInterClusterEdge = value;
    }

    private List<Id<AbstractNode>> __InnerLowerLevelPath = new List<Id<AbstractNode>>();
    public List<Id<AbstractNode>> getInnerLowerLevelPath() {
        return __InnerLowerLevelPath;
    }

    public void setInnerLowerLevelPath(List<Id<AbstractNode>> value) {
        __InnerLowerLevelPath = value;
    }

    public AbstractEdgeInfo(int cost, int level, boolean interCluster) throws Exception {
        setCost(cost);
        setLevel(level);
        setIsInterClusterEdge(interCluster);
    }

    public String toString() {
        try
        {
            return ("cost: " + getCost() + "; level: " + getLevel() + "; interCluster: " + getIsInterClusterEdge());
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
