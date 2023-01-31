//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp;

import HPASharp.Graph.AbstractNode;
import HPASharp.Infrastructure.Id;
import HPASharp.IPathNode;

public class AbstractPathNode   implements IPathNode
{
    public AbstractPathNode() {
    }

    public Id<AbstractNode> Id;
    public int Level = new int();
    public AbstractPathNode(Id<AbstractNode> id, int lvl) throws Exception {
        Id = id;
        Level = lvl;
    }

    public int getIdValue() throws Exception {
        return Id.getIdValue();
    }

}
