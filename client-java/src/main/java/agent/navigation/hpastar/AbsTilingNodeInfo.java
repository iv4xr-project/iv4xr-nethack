//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp;

import HPASharp.Position;

// implements nodes in the abstract graph
public class AbsTilingNodeInfo
{
    private int __Id = new int();
    public int getId() {
        return __Id;
    }

    public void setId(int value) {
        __Id = value;
    }

    private Position __Position = new Position();
    public Position getPosition() {
        return __Position;
    }

    public void setPosition(Position value) {
        __Position = value;
    }

    private int __ClusterId = new int();
    public int getClusterId() {
        return __ClusterId;
    }

    public void setClusterId(int value) {
        __ClusterId = value;
    }

    private int __CenterId = new int();
    public int getCenterId() {
        return __CenterId;
    }

    public void setCenterId(int value) {
        __CenterId = value;
    }

    private int __Level = new int();
    public int getLevel() {
        return __Level;
    }

    public void setLevel(int value) {
        __Level = value;
    }

    private int __LocalIdxCluster = new int();
    public int getLocalIdxCluster() {
        return __LocalIdxCluster;
    }

    public void setLocalIdxCluster(int value) {
        __LocalIdxCluster = value;
    }

    public AbsTilingNodeInfo(int id, int level, int clId, Position position, int centerId, int localIdxCluster) throws Exception {
        setId(id);
        setLevel(level);
        setClusterId(clId);
        setPosition(position);
        setCenterId(centerId);
        setLocalIdxCluster(localIdxCluster);
    }

    public void printInfo() throws Exception {
        Console.Write("id: " + getId());
        Console.Write("; level: " + getLevel());
        Console.Write("; cluster: " + getClusterId());
        Console.Write("; row: " + getPosition().Y);
        Console.Write("; col: " + getPosition().X);
        Console.Write("; center: " + getCenterId());
        Console.Write("; local idx: " + getLocalIdxCluster());
        Console.WriteLine();
    }

}
