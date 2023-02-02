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
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import nethack.object.Level;
import nl.uu.cs.aplib.utils.Pair;

public class Sandbox {
  public static void main(String[] args) {
    int clusterSize = 8;
    int maxLevel = 2;
    int height = Level.HEIGHT;
    int width = Level.WIDTH;

    IPassability passability = new FakePassability(width, height);
    ConcreteMap concreteMap =
        ConcreteMapFactory.createConcreteMap(width, height, passability, TileType.Octile);
    HierarchicalMapFactory abstractMapFactory = new HierarchicalMapFactory();
    HierarchicalMap absTiling =
        abstractMapFactory.createHierarchicalMap(
            concreteMap, clusterSize, maxLevel, EntranceStyle.EndEntrance);
    Function<Pair<IntVec2D, IntVec2D>, List<IPathNode>> doHierarchicalSearch =
        (positions) ->
            hierarchicalSearch(absTiling, maxLevel, concreteMap, positions.fst, positions.snd);

    Function<List<IPathNode>, List<IntVec2D>> toPositionPath =
        (path) -> {
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
        };
    List<Pair<IntVec2D, IntVec2D>> points =
        IntStream.range(0, 2000)
            .mapToObj(
                i -> {
                  IntVec2D pos1 = ((FakePassability) passability).getRandomFreePosition();
                  IntVec2D pos2 = ((FakePassability) passability).getRandomFreePosition();
                  while (Math.abs(pos1.x - pos2.x) + Math.abs(pos1.y - pos2.y) < 10) {
                    pos2 = ((FakePassability) passability).getRandomFreePosition();
                  }
                  return new Pair<IntVec2D, IntVec2D>(pos1, pos2);
                })
            .collect(Collectors.toList());

    long t1 = System.nanoTime();
    for (int i = 0; i < points.size(); i++) {
      IntVec2D startPosition2 = points.get(i).fst;
      IntVec2D endPosition2 = points.get(i).snd;
      List<IPathNode> regularSearchPath =
          doHierarchicalSearch.apply(new Pair<>(startPosition2, endPosition2));
      List<IntVec2D> posPath1 = toPositionPath.apply(regularSearchPath);
    }
    long t2 = System.nanoTime();
    long regularSearchTime = t2 - t1;
    System.out.println(regularSearchTime);
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
    int maxPathsToRefine = Integer.MAX_VALUE;
    HierarchicalSearch hierarchicalSearch = new HierarchicalSearch();
    List<AbstractPathNode> abstractPath =
        hierarchicalSearch.doHierarchicalSearch(
            hierarchicalMap, startAbsNode, targetAbsNode, maxLevel, maxPathsToRefine);
    List<IPathNode> path =
        hierarchicalSearch.abstractPathToLowLevelPath(
            hierarchicalMap, abstractPath, hierarchicalMap.width, maxPathsToRefine);
    SmoothWizard smoother = new SmoothWizard(concreteMap, path);
    path = smoother.smoothPath();
    factory.removeAbstractNode(hierarchicalMap, targetAbsNode);
    factory.removeAbstractNode(hierarchicalMap, startAbsNode);
    return path;
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
    for (int y = 0; y < concreteMap.height; y++) {
      if (y % clusterSize == 0)
        System.out.println("---------------------------------------------------------");

      for (int x = 0; x < concreteMap.width; x++) {
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
