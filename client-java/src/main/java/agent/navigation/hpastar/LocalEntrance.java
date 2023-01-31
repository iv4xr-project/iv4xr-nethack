//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp;

import HPASharp.Position;

public class LocalEntrance
{
    private int __Id = new int();
    public int getId() {
        return __Id;
    }

    public void setId(int value) {
        __Id = value;
    }

    // id of the global abstract node
    private int __AbsNodeId = new int();
    public int getAbsNodeId() {
        return __AbsNodeId;
    }

    public void setAbsNodeId(int value) {
        __AbsNodeId = value;
    }

    // Relative position of entrance inside cluster
    private Position __RelativePos = new Position();
    public Position getRelativePos() {
        return __RelativePos;
    }

    public void setRelativePos(Position value) {
        __RelativePos = value;
    }

    private int __EntranceLocalIdx = new int();
    public int getEntranceLocalIdx() {
        return __EntranceLocalIdx;
    }

    public void setEntranceLocalIdx(int value) {
        __EntranceLocalIdx = value;
    }

    // local id
    public LocalEntrance(int nodeId, int absNodeId, int localIdx, Position relativePosition) throws Exception {
        setId(nodeId);
        setAbsNodeId(absNodeId);
        setEntranceLocalIdx(localIdx);
        setRelativePos(relativePosition);
    }

}
