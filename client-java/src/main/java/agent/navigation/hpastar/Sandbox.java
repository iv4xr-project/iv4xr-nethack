//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:35
//

package agent.navigation.hpastar;

import agent.navigation.hpastar.factories.ConcreteMapFactory;
import agent.navigation.hpastar.factories.EntranceStyle;
import agent.navigation.hpastar.factories.HierarchicalMapFactory;
import agent.navigation.hpastar.graph.AbstractNode;
import agent.navigation.hpastar.graph.ConcreteNode;
import agent.navigation.hpastar.infrastructure.Id;
import agent.navigation.hpastar.passabilities.FakePassability;
import agent.navigation.hpastar.search.HierarchicalSearch;
import agent.navigation.hpastar.smoother.SmoothWizard;
import agent.navigation.strategy.NavUtils;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import nl.uu.cs.aplib.utils.Pair;

public class Sandbox {
  public static void main(String[] args) {
    int clusterSize = 8;
    int maxLevel = 1;
    Size size = new Size(128, 128);

    FakePassability passability = new FakePassability(size);
    ConcreteMap concreteMap =
        ConcreteMapFactory.createConcreteMap(size, passability, TileType.Octile);
    HierarchicalMap absTiling =
        new HierarchicalMapFactory()
            .createHierarchicalMap(
                concreteMap, clusterSize, maxLevel, EntranceStyle.EndEntrance, size);

    List<Pair<IntVec2D, IntVec2D>> points =
        IntStream.range(0, 2000)
            .mapToObj(
                i -> {
                  IntVec2D pos1 = passability.getRandomFreePosition();
                  IntVec2D pos2 = passability.getRandomFreePosition();
                  while (Math.abs(pos1.x - pos2.x) + Math.abs(pos1.y - pos2.y) < 10) {
                    pos2 = passability.getRandomFreePosition();
                  }
                  return new Pair<IntVec2D, IntVec2D>(pos1, pos2);
                })
            .collect(Collectors.toList());

    long t1 = System.nanoTime();
    for (int i = 0; i < points.size(); i++) {
      IntVec2D startPosition = points.get(i).fst;
      IntVec2D endPosition = points.get(i).snd;
      List<IPathNode> regularSearchPath =
          hierarchicalSearch(absTiling, maxLevel, concreteMap, startPosition, endPosition);
      List<IntVec2D> posPath = toPositionPath(regularSearchPath, concreteMap, absTiling);
      verifyPosPath(posPath);
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
    return path;
  }

  private static List<IntVec2D> toPositionPath(
      List<IPathNode> path, ConcreteMap concreteMap, HierarchicalMap absTiling) {
    return path.stream()
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
  }

  private static void verifyPosPath(List<IntVec2D> posPath) {
    if (posPath.size() <= 1) {
      return;
    }
    IntVec2D prevPos = posPath.get(0);
    for (int i = 1; i < posPath.size(); i++) {
      IntVec2D currentPos = posPath.get(i);
      assert NavUtils.adjacent(prevPos, currentPos, true) : "Not all nodes are adjacent";
      prevPos = currentPos;
    }
  }

  private static List<Character> getCharVector(ConcreteMap concreteMap) {
    List<Character> result = new ArrayList<>();
    int numberNodes = concreteMap.getNrNodes();
    for (int i = 0; i < numberNodes; i++) {
      result.add(
          concreteMap.graph.getNodeInfo(new Id<ConcreteNode>().from(i)).isObstacle ? '@' : '.');
    }
    return result;
  }

  public static void printFormatted(
      ConcreteMap concreteMap,
      HierarchicalMap hierarchicalGraph,
      int clusterSize,
      List<IntVec2D> path) {
    printFormatted(getCharVector(concreteMap), concreteMap, hierarchicalGraph, clusterSize, path);
  }

  private static void printFormatted(
      List<Character> chars,
      ConcreteMap concreteMap,
      HierarchicalMap hierarchicalGraph,
      int clusterSize,
      List<IntVec2D> path) {
    for (int y = 0; y < concreteMap.size.height; y++) {
      if (y % clusterSize == 0)
        System.out.println("---------------------------------------------------------");

      for (int x = 0; x < concreteMap.size.width; x++) {
        if (x % clusterSize == 0) System.out.print('|');

        Id<ConcreteNode> nodeId = concreteMap.getNodeIdFromPos(x, y);

        int x0 = x;
        int y0 = y;
        System.out.print(
            path.stream()
                    .anyMatch(
                        (node) -> {
                          return node.x == x0 && node.x == y0;
                        })
                ? 'X'
                : chars.get(nodeId.getIdValue()));
      }
      System.out.println();
    }
  }
}
