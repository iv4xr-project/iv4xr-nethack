//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp;

import HPASharp.Infrastructure.Id;

public class Connection <TNode>
{
    public Connection() {
    }

    public Id<TNode> Target;
    public int Cost = new int();
    public Connection(Id<TNode> target, int cost) throws Exception {
        Target = target;
        Cost = cost;
    }

}
