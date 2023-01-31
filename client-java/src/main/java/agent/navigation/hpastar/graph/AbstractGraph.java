//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar.graph;

import agent.navigation.hpastar.infrastructure.Id;
import nl.uu.cs.aplib.utils.Pair;

public class AbstractGraph
    extends Graph<AbstractNode, AbstractNodeInfo, AbstractEdge, AbstractEdgeInfo> {
  public AbstractGraph() {
    super(
        (Pair<Id<AbstractNode>, AbstractNodeInfo> pair) -> {
          return new AbstractNode(pair.fst, pair.snd);
        },
        (Pair<Id<AbstractNode>, AbstractEdgeInfo> pair) -> {
          return new AbstractEdge(pair.fst, pair.snd);
        });
  }
}
