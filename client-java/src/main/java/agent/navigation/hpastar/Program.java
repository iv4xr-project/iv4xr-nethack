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
import agent.navigation.hpastar.search.IdPath;
import agent.navigation.hpastar.smoother.SmoothWizard;
import agent.navigation.hpastar.utils.RefSupport;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import nl.uu.cs.aplib.utils.Pair;
import util.ColoredStringBuilder;
import util.CustomVec2D;
import util.Stopwatch;

public class Program {

  public static class ExamplePassability implements IPassability {

    public ExamplePassability() {
      obstacles = new boolean[40][40];
      String map =
          """
              \r
                                  0000000000000000000100000000000000000000\r
                                  0111111111111111111111111111111111111110\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0001000000000000000100000000000000000000\r
                                  0001000000000000000100000000000000000000\r
                                  0001000000000000000100000000000000000000\r
                                  0001000000000000000100000000000000000000\r
                                  0001000000000000000100000000000000000000\r
                                  0001000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0111111111111111111100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0000000000000000000100000000000000000000\r
                                  0111111111111111111111111111111111111110\r
                                  0000000000000000000000000000000000000000\r
                             \s""";
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

    final Random rnd = new Random(700);

    public CustomVec2D getRandomFreePosition() {
      int x = rnd.nextInt(40);
      int y = rnd.nextInt(40);
      while (obstacles[x][y]) {
        x = rnd.nextInt(40);
        y = rnd.nextInt(40);
      }
      return new CustomVec2D(x, y);
    }

    private final boolean[][] obstacles;

    @Override
    public void updateCanMoveDiagonally(CustomVec2D pos, boolean canMoveDiagonally) {}

    @Override
    public void updateObstacle(CustomVec2D pos, boolean isObstacle) {}

    public boolean cannotEnter(CustomVec2D pos, RefSupport<Integer> cost) {
      cost.setValue(Constants.COST_ONE);
      return obstacles[pos.y][pos.x];
    }

    public boolean cannotEnter(CustomVec2D pos) {
      return obstacles[pos.y][pos.x];
    }

    @Override
    public boolean canMoveDiagonal(CustomVec2D pos1, CustomVec2D pos2) {
      return true;
    }

    @Override
    public boolean canMoveDiagonal(CustomVec2D pos) {
      return false;
    }

    @Override
    public ConcreteMap slice(int horizOrigin, int vertOrigin, Size size) {
      return null;
    }

    @Override
    public ConcreteMap getConcreteMap() {
      return null;
    }
  }

  public static void main6(String[] args) {
    int clusterSize = 8;
    int maxLevel = 2;
    Size size = new Size(70, 70);

    CustomVec2D startPosition = new CustomVec2D(1, 0);
    CustomVec2D endPosition = new CustomVec2D(69, 69);
    // Prepare the abstract graph beforehand
    IPassability passability = new FakePassability(size, true);
    ConcreteMap concreteMap =
        ConcreteMapFactory.createConcreteMap(size, passability, NavType.Octile);
    HierarchicalMapFactory abstractMapFactory = new HierarchicalMapFactory();
    HierarchicalMap absTiling =
        abstractMapFactory.createHierarchicalMap(
            concreteMap, clusterSize, maxLevel, EntranceStyle.EndEntrance, size);
    // var edges = absTiling.AbstractGraph.Nodes.SelectMany(x => x.Edges.Values)
    //    .GroupBy(x => x.Info.Level)
    //    .ToDictionary(x => x.Key, x => x.Count());
    Stopwatch stopwatch = new Stopwatch(true);
    List<IPathNode> regularSearchPath = regularSearch(concreteMap, startPosition, endPosition);
    float regularSearchTime = stopwatch.split();
    List<IPathNode> hierarchicalSearchPath =
        hierarchicalSearch(absTiling, maxLevel, concreteMap, startPosition, endPosition);
    float hierarchicalSearchTime = stopwatch.split();
    List<CustomVec2D> pospath =
        hierarchicalSearchPath.stream()
            .map(
                p -> {
                  if (p instanceof ConcretePathNode concretePathNode) {
                    return concreteMap.graph.getNodeInfo(concretePathNode.id).position;
                  }

                  AbstractPathNode abstractPathNode = (AbstractPathNode) p;
                  return absTiling.abstractGraph.getNodeInfo(abstractPathNode.id).position;
                })
            .toList();

    ColoredStringBuilder csb = new ColoredStringBuilder();
    csb.appendf("Regular search: %d ms%n", regularSearchTime);
    csb.appendf("Number of nodes: %d%n", regularSearchPath.size());
    csb.appendf("Hierarchical search: %d ms%n", hierarchicalSearchTime);
    csb.appendf("Number of nodes: %d%n", hierarchicalSearchPath.size());
    System.out.print(csb);
  }

  public static void main2(String[] args) {
    int clusterSize = 8;
    int maxLevel = 2;
    Size size = new Size(128, 128);

    CustomVec2D startPosition = new CustomVec2D(17, 38);
    CustomVec2D endPosition = new CustomVec2D(16, 18);
    // Prepare the abstract graph beforehand
    IPassability passability = new FakePassability(size, true);
    ConcreteMap concreteMap =
        ConcreteMapFactory.createConcreteMap(size, passability, NavType.Octile);
    HierarchicalMapFactory abstractMapFactory = new HierarchicalMapFactory();
    HierarchicalMap absTiling =
        abstractMapFactory.createHierarchicalMap(
            concreteMap, clusterSize, maxLevel, EntranceStyle.EndEntrance, size);
    Stopwatch stopwatch = new Stopwatch(true);
    List<IPathNode> regularSearchPath = regularSearch(concreteMap, startPosition, endPosition);
    float regularSearchTime = stopwatch.split();
    List<IPathNode> hierarchicalSearchPath =
        hierarchicalSearch(absTiling, maxLevel, concreteMap, startPosition, endPosition);
    float hierarchicalSearchTime = stopwatch.split();
    List<CustomVec2D> pospath =
        hierarchicalSearchPath.stream()
            .map(
                p -> {
                  if (p instanceof ConcretePathNode concretePathNode) {
                    return concreteMap.graph.getNodeInfo(concretePathNode.id).position;
                  }

                  AbstractPathNode abstractPathNode = (AbstractPathNode) p;
                  return absTiling.abstractGraph.getNodeInfo(abstractPathNode.id).position;
                })
            .toList();

    ColoredStringBuilder csb = new ColoredStringBuilder();
    csb.appendf("Regular search: %d ms%n", regularSearchTime);
    csb.appendf("Number of nodes: %d%n", regularSearchPath.size());
    csb.appendf("Hierarchical search: %d ms%n", hierarchicalSearchTime);
    csb.appendf("Number of nodes: %d%n", hierarchicalSearchPath.size());
    System.out.print(csb);
  }

  public static void main(String[] args) {
    Program.Main(args);
  }

  public static void Main(String[] args) {
    int clusterSize = 8;
    int maxLevel = 2;
    Size size = new Size(128, 128);

    // IPassability passability = new ExamplePassability();
    FakePassability passability = new FakePassability(size, true);
    ConcreteMap concreteMap =
        ConcreteMapFactory.createConcreteMap(size, passability, NavType.Octile);
    HierarchicalMapFactory abstractMapFactory = new HierarchicalMapFactory();
    HierarchicalMap absTiling =
        abstractMapFactory.createHierarchicalMap(
            concreteMap, clusterSize, maxLevel, EntranceStyle.EndEntrance, size);
    // var edges = absTiling.AbstractGraph.Nodes.SelectMany(x => x.Edges.Values)
    //    .GroupBy(x => x.Info.Level)
    //    .ToDictionary(x => x.Key, x => x.Count());
    Function<Pair<CustomVec2D, CustomVec2D>, List<IPathNode>> doHierarchicalSearch =
        (positions) ->
            hierarchicalSearch(absTiling, maxLevel, concreteMap, positions.fst, positions.snd);
    Function<Pair<CustomVec2D, CustomVec2D>, List<IPathNode>> doRegularSearch =
        (positions) -> regularSearch(concreteMap, positions.fst, positions.snd);

    Function<List<IPathNode>, List<CustomVec2D>> toPositionPath =
        (path) ->
            path.stream()
                .map(
                    (p) -> {
                      if (p instanceof ConcretePathNode concretePathNode) {
                        return concreteMap.graph.getNodeInfo(concretePathNode.id).position;
                      }

                      AbstractPathNode abstractPathNode = (AbstractPathNode) p;
                      return absTiling.abstractGraph.getNodeInfo(abstractPathNode.id).position;
                    })
                .collect(Collectors.toList());
    List<Pair<CustomVec2D, CustomVec2D>> points =
        IntStream.range(0, 2000)
            .mapToObj(
                i -> {
                  CustomVec2D pos1 = passability.getRandomFreePosition();
                  CustomVec2D pos2 = passability.getRandomFreePosition();
                  while (Math.abs(pos1.x - pos2.x) + Math.abs(pos1.y - pos2.y) < 10) {
                    pos2 = passability.getRandomFreePosition();
                  }
                  return new Pair<>(pos1, pos2);
                })
            .toList();

    List<Function<Pair<CustomVec2D, CustomVec2D>, List<IPathNode>>> searchStrategies =
        new ArrayList<>(2);
    searchStrategies.add(doRegularSearch);
    searchStrategies.add(doHierarchicalSearch);

    for (Function<Pair<CustomVec2D, CustomVec2D>, List<IPathNode>> searchStrategy :
        searchStrategies) {
      Stopwatch stopwatch = new Stopwatch(true);
      for (Pair<CustomVec2D, CustomVec2D> point : points) {
        CustomVec2D startPosition2 = point.fst;
        CustomVec2D endPosition2 = point.snd;
        List<IPathNode> regularSearchPath =
            searchStrategy.apply(new Pair<>(startPosition2, endPosition2));
        List<CustomVec2D> posPath1 = toPositionPath.apply(regularSearchPath);
        System.out.println(posPath1);
      }
      float regularSearchTime = stopwatch.split();
      System.out.println(regularSearchTime);
    }
  }

  private static List<IPathNode> hierarchicalSearch(
      HierarchicalMap hierarchicalMap,
      int maxLevel,
      ConcreteMap concreteMap,
      CustomVec2D startPosition,
      CustomVec2D endPosition) {
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
            hierarchicalMap, abstractPath, hierarchicalMap.size.width, maxPathsToRefine);
    SmoothWizard smoother = new SmoothWizard(concreteMap, path);
    path = smoother.smoothConcretePath();
    factory.removeAbstractNode(hierarchicalMap, targetAbsNode);
    factory.removeAbstractNode(hierarchicalMap, startAbsNode);
    return path;
  }

  private static List<IPathNode> regularSearch(
      ConcreteMap concreteMap, CustomVec2D startPosition, CustomVec2D endPosition) {
    ConcreteGraph tilingGraph = concreteMap.graph;
    Function<Pair<Integer, Integer>, ConcreteNode> getNode =
        (pos) -> tilingGraph.getNode(concreteMap.getNodeIdFromPos(pos.fst, pos.snd));

    // Regular pathfinding
    AStar<ConcreteNode> searcher =
        new AStar<>(
            concreteMap,
            getNode.apply(new Pair<>(startPosition.x, startPosition.y)).nodeId,
            getNode.apply(new Pair<>(endPosition.x, endPosition.y)).nodeId);
    IdPath<ConcreteNode> idPath = searcher.findPath();
    List<Id<ConcreteNode>> path2 = Objects.requireNonNull(idPath).pathNodes;
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
      List<CustomVec2D> path) {
    printFormatted(getCharVector(concreteMap), concreteMap, hierarchicalGraph, clusterSize, path);
  }

  private static void printFormatted(
      List<Character> chars,
      ConcreteMap concreteMap,
      HierarchicalMap hierarchicalGraph,
      int clusterSize,
      List<CustomVec2D> path) {
    ColoredStringBuilder csb = new ColoredStringBuilder();
    for (int y = 0; y < concreteMap.size.height; y++) {
      if (y % clusterSize == 0) {
        csb.append("---------------------------------------------------------").newLine();
      }

      for (int x = 0; x < concreteMap.size.width; x++) {
        //                Console.ForegroundColor = ConsoleColor.White;
        if (x % clusterSize == 0) {
          csb.append('|');
        }

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
        csb.append(
            path.stream().anyMatch((node) -> node.x == x0 && node.x == y0)
                ? 'X'
                : chars.get(nodeId.getIdValue()));
      }
      csb.newLine();
    }
    System.out.print(csb);
  }
}
