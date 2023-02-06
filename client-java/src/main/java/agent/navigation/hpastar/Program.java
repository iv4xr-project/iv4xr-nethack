//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:35
//

package agent.navigation.hpastar;

import agent.navigation.hpastar.factories.ConcreteMapFactory;
import agent.navigation.hpastar.factories.EntranceStyle;
import agent.navigation.hpastar.factories.HierarchicalMapFactory;
import agent.navigation.hpastar.graph.AbstractNode;
import agent.navigation.hpastar.graph.ConcreteGraph;
import agent.navigation.hpastar.graph.ConcreteNode;
import agent.navigation.hpastar.infrastructure.Constants;
import agent.navigation.hpastar.infrastructure.Id;
import agent.navigation.hpastar.passabilities.FakePassability;
import agent.navigation.hpastar.search.AStar;
import agent.navigation.hpastar.search.HierarchicalSearch;
import agent.navigation.hpastar.search.Path;
import agent.navigation.hpastar.smoother.SmoothWizard;
import agent.navigation.hpastar.utils.RefSupport;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import nl.uu.cs.aplib.utils.Pair;

public class Program {

  public static class ExamplePassability implements IPassability {
    private String map =
        "\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0111111111111111111111111111111111111110\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0001000000000000000100000000000000000000\r\n"
            + "                    0001000000000000000100000000000000000000\r\n"
            + "                    0001000000000000000100000000000000000000\r\n"
            + "                    0001000000000000000100000000000000000000\r\n"
            + "                    0001000000000000000100000000000000000000\r\n"
            + "                    0001000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0111111111111111111100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0000000000000000000100000000000000000000\r\n"
            + "                    0111111111111111111111111111111111111110\r\n"
            + "                    0000000000000000000000000000000000000000\r\n"
            + "                ";

    public ExamplePassability() {
      obstacles = new boolean[40][40];
      char[][] charlines =
          (char[][])
              Arrays.stream(map.split("\n")).map(String::trim).map(String::toCharArray).toArray();

      //            " */).Where(/* [UNSUPPORTED] to translate lambda expressions we need an explicit
      // delegate type, try adding a cast "(line) => {
      //                return !String.IsNullOrWhiteSpace(line);

      for (int j = 0; j < obstacles[0].length; j++)
        for (int i = 0; i < obstacles.length; i++) {
          obstacles[i][j] = charlines[i][j] == '1';
        }
    }

    Random rnd = new Random(700);

    public IntVec2D getRandomFreePosition() {
      int x = rnd.nextInt(40);
      int y = rnd.nextInt(40);
      while (obstacles[x][y]) {
        x = rnd.nextInt(40);
        y = rnd.nextInt(40);
      }
      return new IntVec2D(x, y);
    }

    private boolean[][] obstacles;

    public boolean canEnter(IntVec2D pos, RefSupport<Integer> cost) {
      cost.setValue(Constants.COST_ONE);
      return !obstacles[pos.y][pos.x];
    }

    @Override
    public boolean canMoveDiagonal(IntVec2D pos1, IntVec2D pos2) {
      return true;
    }
  }

  public static void main6(String[] args) {
    int clusterSize = 8;
    int maxLevel = 2;
    int height = 70;
    int width = 70;

    IntVec2D startPosition = new IntVec2D(1, 0);
    IntVec2D endPosition = new IntVec2D(69, 69);
    // Prepare the abstract graph beforehand
    IPassability passability = new FakePassability(width, height);
    ConcreteMap concreteMap =
        ConcreteMapFactory.createConcreteMap(width, height, passability, TileType.Octile);
    HierarchicalMapFactory abstractMapFactory = new HierarchicalMapFactory();
    HierarchicalMap absTiling =
        abstractMapFactory.createHierarchicalMap(
            concreteMap, clusterSize, maxLevel, EntranceStyle.EndEntrance);
    // var edges = absTiling.AbstractGraph.Nodes.SelectMany(x => x.Edges.Values)
    //    .GroupBy(x => x.Info.Level)
    //    .ToDictionary(x => x.Key, x => x.Count());
    long t1 = System.nanoTime();
    List<IPathNode> regularSearchPath = regularSearch(concreteMap, startPosition, endPosition);
    long t2 = System.nanoTime();
    long regularSearchTime = t2 - t1;
    List<IPathNode> hierarchicalSearchPath =
        hierarchicalSearch(absTiling, maxLevel, concreteMap, startPosition, endPosition);
    long t3 = System.nanoTime();
    long hierarchicalSearchTime = t3 - t2;
    List<IntVec2D> pospath =
        hierarchicalSearchPath.stream()
            .map(
                p -> {
                  if (p instanceof ConcretePathNode) {
                    ConcretePathNode concretePathNode = (ConcretePathNode) p;
                    return concreteMap.graph.getNodeInfo(concretePathNode.id).position;
                  }

                  AbstractPathNode abstractPathNode = (AbstractPathNode) p;
                  return absTiling.abstractGraph.getNodeInfo(abstractPathNode.id).position;
                })
            .collect(Collectors.toList());
    System.out.println("Regular search: " + regularSearchTime + " ms");
    System.out.println("Number of nodes: " + regularSearchPath.size());
    System.out.println("Hierachical search: " + hierarchicalSearchTime + " ms");
    System.out.println("Number of nodes: " + hierarchicalSearchPath.size());
  }

  public static void main2(String[] args) {
    int clusterSize = 8;
    int maxLevel = 2;
    int height = 128;
    int width = 128;

    IntVec2D startPosition = new IntVec2D(17, 38);
    IntVec2D endPosition = new IntVec2D(16, 18);
    // Prepare the abstract graph beforehand
    IPassability passability = new FakePassability(width, height);
    ConcreteMap concreteMap =
        ConcreteMapFactory.createConcreteMap(width, height, passability, TileType.Octile);
    HierarchicalMapFactory abstractMapFactory = new HierarchicalMapFactory();
    HierarchicalMap absTiling =
        abstractMapFactory.createHierarchicalMap(
            concreteMap, clusterSize, maxLevel, EntranceStyle.EndEntrance);
    long t1 = System.nanoTime();
    List<IPathNode> regularSearchPath = regularSearch(concreteMap, startPosition, endPosition);
    long t2 = System.nanoTime();
    long regularSearchTime = t2 - t1;
    List<IPathNode> hierarchicalSearchPath =
        hierarchicalSearch(absTiling, maxLevel, concreteMap, startPosition, endPosition);
    long t3 = System.nanoTime();
    long hierarchicalSearchTime = t3 - t2;
    List<IntVec2D> pospath =
        hierarchicalSearchPath.stream()
            .map(
                p -> {
                  if (p instanceof ConcretePathNode) {
                    ConcretePathNode concretePathNode = (ConcretePathNode) p;
                    return concreteMap.graph.getNodeInfo(concretePathNode.id).position;
                  }

                  AbstractPathNode abstractPathNode = (AbstractPathNode) p;
                  return absTiling.abstractGraph.getNodeInfo(abstractPathNode.id).position;
                })
            .collect(Collectors.toList());
    System.out.println("Regular search: " + regularSearchTime + " ms");
    System.out.println("Number of nodes: " + regularSearchPath.size());
    System.out.println("Hierachical search: " + hierarchicalSearchTime + " ms");
    System.out.println("Number of nodes: " + hierarchicalSearchPath.size());
  }

  public static void main(String[] args) {
    Program.Main(args);
  }

  public static void Main(String[] args) {
    int clusterSize = 8;
    int maxLevel = 2;
    int height = 128;
    int width = 128;

    // IPassability passability = new ExamplePassability();
    IPassability passability = new FakePassability(width, height);
    ConcreteMap concreteMap =
        ConcreteMapFactory.createConcreteMap(width, height, passability, TileType.Octile);
    HierarchicalMapFactory abstractMapFactory = new HierarchicalMapFactory();
    HierarchicalMap absTiling =
        abstractMapFactory.createHierarchicalMap(
            concreteMap, clusterSize, maxLevel, EntranceStyle.EndEntrance);
    // var edges = absTiling.AbstractGraph.Nodes.SelectMany(x => x.Edges.Values)
    //    .GroupBy(x => x.Info.Level)
    //    .ToDictionary(x => x.Key, x => x.Count());
    Function<Pair<IntVec2D, IntVec2D>, List<IPathNode>> doHierarchicalSearch =
        (positions) ->
            hierarchicalSearch(absTiling, maxLevel, concreteMap, positions.fst, positions.snd);
    Function<Pair<IntVec2D, IntVec2D>, List<IPathNode>> doRegularSearch =
        (positions) -> regularSearch(concreteMap, positions.fst, positions.snd);

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

    List<Function<Pair<IntVec2D, IntVec2D>, List<IPathNode>>> searchStrategies =
        new ArrayList<Function<Pair<IntVec2D, IntVec2D>, List<IPathNode>>>(2);
    searchStrategies.add(doRegularSearch);
    searchStrategies.add(doHierarchicalSearch);

    for (Function<Pair<IntVec2D, IntVec2D>, List<IPathNode>> searchStrategy : searchStrategies) {
      long t1 = System.nanoTime();
      for (int i = 0; i < points.size(); i++) {
        IntVec2D startPosition2 = points.get(i).fst;
        IntVec2D endPosition2 = points.get(i).snd;
        List<IPathNode> regularSearchPath =
            searchStrategy.apply(new Pair<>(startPosition2, endPosition2));
        List<IntVec2D> posPath1 = toPositionPath.apply(regularSearchPath);
        System.out.println(posPath1);
      }
      long t2 = System.nanoTime();
      long regularSearchTime = t2 - t1;
      System.out.println(regularSearchTime);
    }
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

  private static List<IPathNode> regularSearch(
      ConcreteMap concreteMap, IntVec2D startPosition, IntVec2D endPosition) {
    ConcreteGraph tilingGraph = concreteMap.graph;
    Function<Pair<Integer, Integer>, ConcreteNode> getNode =
        (pos) -> tilingGraph.getNode(concreteMap.getNodeIdFromPos(pos.fst, pos.snd));

    // Regular pathfinding
    AStar<ConcreteNode> searcher =
        new AStar<>(
            concreteMap,
            getNode.apply(new Pair<>(startPosition.x, startPosition.y)).nodeId,
            getNode.apply(new Pair<>(endPosition.x, endPosition.y)).nodeId);
    Path<ConcreteNode> path = searcher.findPath();
    List<Id<ConcreteNode>> path2 = path.pathNodes;
    return path2.stream()
        .map(p -> (IPathNode) new ConcretePathNode(p))
        .collect(Collectors.toList());
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
        //                Console.ForegroundColor = ConsoleColor.White;
        if (x % clusterSize == 0) System.out.print('|');

        Id<ConcreteNode> nodeId = concreteMap.getNodeIdFromPos(x, y);
        //                /* [UNSUPPORTED] 'var' as type is unsupported "var" */ hasAbsNode =
        // hierarchicalGraph.abstractGraph.nodes.SingleOrDefault(/* [UNSUPPORTED] to translate
        // lambda expressions we need an explicit delegate type, try adding a cast "(n) => {
        //                    return n.Info.ConcreteNodeId == nodeId;
        //                }" */);
        //                if (hasAbsNode != null)
        //                {
        //                    Level __dummyScrutVar0 = hasAbsNode.Info.Level;
        //                    if (__dummyScrutVar0.equals(1))
        //                    {
        //                        Console.ForegroundColor = ConsoleColor.Red;
        //                    }
        //                    else if (__dummyScrutVar0.equals(2))
        //                    {
        //                        Console.ForegroundColor = ConsoleColor.DarkGreen;
        //                    }
        //
        //                }

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
