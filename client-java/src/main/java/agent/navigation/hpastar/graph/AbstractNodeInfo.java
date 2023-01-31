//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp.Graph;

import HPASharp.Cluster;
import HPASharp.Graph.AbstractNode;
import HPASharp.Graph.ConcreteNode;
import HPASharp.Infrastructure.Id;
import HPASharp.Position;

// implements nodes in the abstract graph
public class AbstractNodeInfo
{
    private Id<AbstractNode> __Id;
    public Id<AbstractNode> getId() {
        return __Id;
    }

    public void setId(Id<AbstractNode> value) {
        __Id = value;
    }

    private Position __Position = new Position();
    public Position getPosition() {
        return __Position;
    }

    public void setPosition(Position value) {
        __Position = value;
    }

    private Id<Cluster> __ClusterId;
    public Id<Cluster> getClusterId() {
        return __ClusterId;
    }

    public void setClusterId(Id<Cluster> value) {
        __ClusterId = value;
    }

    private Id<ConcreteNode> __ConcreteNodeId;
    public Id<ConcreteNode> getConcreteNodeId() {
        return __ConcreteNodeId;
    }

    public void setConcreteNodeId(Id<ConcreteNode> value) {
        __ConcreteNodeId = value;
    }

    private int __Level = new int();
    public int getLevel() {
        return __Level;
    }

    public void setLevel(int value) {
        __Level = value;
    }

    public AbstractNodeInfo(Id<AbstractNode> id, int level, Id<Cluster> clId, Position position, Id<ConcreteNode> concreteNodeId) throws Exception {
        setId(id);
        setLevel(level);
        setClusterId(clId);
        setPosition(position);
        setConcreteNodeId(concreteNodeId);
    }

    public void printInfo() throws Exception {
        Console.Write("id: " + getId());
        Console.Write("; level: " + getLevel());
        Console.Write("; cluster: " + getClusterId());
        Console.Write("; row: " + getPosition().Y);
        Console.Write("; col: " + getPosition().X);
        Console.Write("; center: " + getConcreteNodeId());
        Console.WriteLine();
    }

}
