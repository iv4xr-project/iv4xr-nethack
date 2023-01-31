//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:33
//

package HPASharp.Factories;

import HPASharp.Cluster;
import HPASharp.Factories.Entrance;
import HPASharp.Graph.ConcreteNode;
import HPASharp.Infrastructure.Id;
import HPASharp.Orientation;

public class Entrance
{
    private Id<Entrance> __Id;
    public Id<Entrance> getId() {
        return __Id;
    }

    public void setId(Id<Entrance> value) {
        __Id = value;
    }

    private Cluster __Cluster1;
    public Cluster getCluster1() {
        return __Cluster1;
    }

    public void setCluster1(Cluster value) {
        __Cluster1 = value;
    }

    private Cluster __Cluster2;
    public Cluster getCluster2() {
        return __Cluster2;
    }

    public void setCluster2(Cluster value) {
        __Cluster2 = value;
    }

    private ConcreteNode __SrcNode;
    public ConcreteNode getSrcNode() {
        return __SrcNode;
    }

    public void setSrcNode(ConcreteNode value) {
        __SrcNode = value;
    }

    private ConcreteNode __DestNode;
    public ConcreteNode getDestNode() {
        return __DestNode;
    }

    public void setDestNode(ConcreteNode value) {
        __DestNode = value;
    }

    private Orientation __Orientation = Orientation.Horizontal;
    public Orientation getOrientation() {
        return __Orientation;
    }

    public void setOrientation(Orientation value) {
        __Orientation = value;
    }

    public Entrance(Id<Entrance> id, Cluster cluster1, Cluster cluster2, ConcreteNode srcNode, ConcreteNode destNode, Orientation orientation) throws Exception {
        setId(id);
        setCluster1(cluster1);
        setCluster2(cluster2);
        setSrcNode(srcNode);
        setDestNode(destNode);
        setOrientation(orientation);
    }

    public int getEntranceLevel(int clusterSize, int maxLevel) throws Exception {
        int level = new int();
        Orientation __dummyScrutVar0 = getOrientation();
        if (__dummyScrutVar0.equals(getOrientation().Horizontal))
        {
            level = DetermineLevel(clusterSize, maxLevel, getSrcNode().getInfo().getPosition().X);
        }
        else if (__dummyScrutVar0.equals(getOrientation().Vertical))
        {
            level = DetermineLevel(clusterSize, maxLevel, getSrcNode().getInfo().getPosition().Y);
        }
        else
        {
            level = -1;
        }
        return level;
    }

    private int determineLevel(int clusterSize, int maxLevel, int y) throws Exception {
        int level = 1;
        if (y % clusterSize != 0)
            y++;

        if (y < clusterSize)
        {
            return 1;
        }

        int clusterY = y / clusterSize;
        while (clusterY % 2 == 0 && level < maxLevel)
        {
            clusterY /= 2;
            level++;
        }
        return level > maxLevel ? maxLevel : level;
    }

}
