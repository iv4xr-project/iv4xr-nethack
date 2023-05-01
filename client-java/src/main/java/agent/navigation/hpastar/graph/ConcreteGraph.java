//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar.graph;

import agent.navigation.hpastar.infrastructure.Id;
import nl.uu.cs.aplib.utils.Pair;

public class ConcreteGraph
    extends Graph<ConcreteNode, ConcreteNodeInfo, ConcreteEdge, ConcreteEdgeInfo> {
  public ConcreteGraph() {
    super(
        (Pair<Id<ConcreteNode>, ConcreteNodeInfo> pair) -> new ConcreteNode(pair.fst, pair.snd),
        (Pair<Id<ConcreteNode>, ConcreteEdgeInfo> pair) -> new ConcreteEdge(pair.fst, pair.snd));
  }
}
