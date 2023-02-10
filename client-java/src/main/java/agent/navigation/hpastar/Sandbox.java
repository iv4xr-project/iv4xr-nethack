//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:35
//

package agent.navigation.hpastar;

import agent.navigation.hpastar.factories.ConcreteMapFactory;
import agent.navigation.hpastar.factories.EntranceStyle;
import agent.navigation.hpastar.factories.HierarchicalMapFactory;
import agent.navigation.hpastar.graph.AbstractNode;
import agent.navigation.hpastar.infrastructure.Id;
import agent.navigation.hpastar.passabilities.FakePassability;
import agent.navigation.hpastar.search.HierarchicalSearch;
import agent.navigation.hpastar.smoother.SmoothWizard;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import nl.uu.cs.aplib.utils.Pair;

public class Sandbox {
  public static void main(String[] args) {
    int clusterSize = 8;
    int maxLevel = 1;
    Size size = new Size(128, 128);

    FakePassability passability = new FakePassability(size, true);
    ConcreteMap concreteMap =
        ConcreteMapFactory.createConcreteMap(size, passability, TileType.Octile);
    HierarchicalMap absTiling =
        new HierarchicalMapFactory()
            .createHierarchicalMap(
                concreteMap, clusterSize, maxLevel, EntranceStyle.EndEntrance, size);
    concreteMap.printFormatted();

    List<Pair<IntVec2D, IntVec2D>> points =
        IntStream.range(0, 4000)
            .mapToObj(
                i -> {
                  IntVec2D pos1 = passability.getRandomFreePosition();
                  IntVec2D pos2 = passability.getRandomFreePosition();
                  while (Math.abs(pos1.x - pos2.x) + Math.abs(pos1.y - pos2.y) < 2) {
                    pos2 = passability.getRandomFreePosition();
                  }
                  return new Pair<IntVec2D, IntVec2D>(pos1, pos2);
                })
            .collect(Collectors.toList());

    //    points.set(0, new Pair<IntVec2D, IntVec2D>(new IntVec2D(3, 1), new IntVec2D(4, 9)));

    long t1 = System.nanoTime();
    for (int i = 0; i < points.size(); i++) {
      IntVec2D startPosition = points.get(i).fst;
      IntVec2D endPosition = points.get(i).snd;
      List<IPathNode> regularSearchPath =
          hierarchicalSearch(absTiling, maxLevel, concreteMap, startPosition, endPosition);
      //      List<IntVec2D> posPath = toPositionPath(regularSearchPath, concreteMap, absTiling);
      //      System.out.printf("%s -> %s %s%n", startPosition, endPosition, posPath);
    }
    long t2 = System.nanoTime();
    long regularSearchTime = t2 - t1;
    System.out.printf(
        "Searching %d paths took: %.2fs%n", points.size(), regularSearchTime / 1000000000.0f);
  }

  private static List<IPathNode> hierarchicalSearch(
      HierarchicalMap hierarchicalMap,
      int maxLevel,
      ConcreteMap concreteMap,
      IntVec2D startPosition,
      IntVec2D endPosition) {
    HierarchicalMapFactory factory = new HierarchicalMapFactory();
    Id<AbstractNode> startAbsNode = factory.insertAbstractNode(hierarchicalMap, startPosition);
    Id<AbstractNode> targetAbsNode = factory.insertAbstractNode(hierarchicalMap, endPosition);
    assert !startAbsNode.equals(targetAbsNode);
    int maxPathsToRefine = Integer.MAX_VALUE;
    HierarchicalSearch hierarchicalSearch = new HierarchicalSearch();
    List<AbstractPathNode> abstractPath =
        hierarchicalSearch.doHierarchicalSearch(
            hierarchicalMap, startAbsNode, targetAbsNode, maxLevel, maxPathsToRefine);
    List<IPathNode> path =
        hierarchicalSearch.abstractPathToLowLevelPath(
            hierarchicalMap, abstractPath, hierarchicalMap.size.width, maxPathsToRefine);

    SmoothWizard smoother = new SmoothWizard(concreteMap, path);
    path = smoother.smoothPath();
    factory.removeAbstractNode(hierarchicalMap, targetAbsNode);
    factory.removeAbstractNode(hierarchicalMap, startAbsNode);
    return null;
  }

  private static List<IntVec2D> toPositionPath(
      List<IPathNode> path, ConcreteMap concreteMap, HierarchicalMap absTiling) {
    List<IntVec2D> posPath =
        path.stream()
            .map(
                (p) -> {
                  if (p instanceof ConcretePathNode) {
                    ConcretePathNode concretePathNode = (ConcretePathNode) p;
                    return concreteMap.graph.getNodeInfo(concretePathNode.id).position;
                  }

                  AbstractPathNode abstractPathNode = (AbstractPathNode) p;
                  return absTiling.abstractGraph.getNodeInfo(abstractPathNode.id).position;
                })
            .collect(Collectors.toList());

    if (posPath.size() <= 1) {
      return posPath;
    }
    //    IntVec2D prevPos = posPath.get(0);
    //    for (int i = 1; i < posPath.size(); i++) {
    //      IntVec2D currentPos = posPath.get(i);
    //      assert NavUtils.adjacent(prevPos, currentPos, true)
    //          : String.format("Non adjacent node error at %s -> %s", prevPos, currentPos);
    //      prevPos = currentPos;
    //    }
    return posPath;
  }
}
