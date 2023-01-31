//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp.Graph;


public class ConcreteEdgeInfo
{
    public ConcreteEdgeInfo(int cost) throws Exception {
        setCost(cost);
    }

    private int __Cost = new int();
    public int getCost() {
        return __Cost;
    }

    public void setCost(int value) {
        __Cost = value;
    }

}
