//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp.Graph;

import HPASharp.Position;

public class ConcreteNodeInfo
{
    public ConcreteNodeInfo(boolean isObstacle, int cost, Position position) throws Exception {
        setIsObstacle(isObstacle);
        setPosition(position);
        setCost(cost);
    }

    private Position __Position = new Position();
    public Position getPosition() {
        return __Position;
    }

    public void setPosition(Position value) {
        __Position = value;
    }

    private boolean __IsObstacle = new boolean();
    public boolean getIsObstacle() {
        return __IsObstacle;
    }

    public void setIsObstacle(boolean value) {
        __IsObstacle = value;
    }

    private int __Cost = new int();
    public int getCost() {
        return __Cost;
    }

    public void setCost(int value) {
        __Cost = value;
    }

}
