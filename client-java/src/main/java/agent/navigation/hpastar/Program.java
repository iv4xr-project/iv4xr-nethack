//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:35
//

package agent.navigation.hpastar;

import CS2JNet.JavaSupport.language.RefSupport;
import agent.navigation.hpastar.infrastructure.Constants;
import agent.navigation.hpastar.Position;
import agent.navigation.hpastar.factories.ConcreteMapFactory;
import agent.navigation.hpastar.factories.EntranceStyle;
import agent.navigation.hpastar.factories.HierarchicalMapFactory;
import agent.navigation.hpastar.passabilities.FakePassability;
import agent.navigation.hpastar.utils.RefSupport;
import eu.iv4xr.framework.spatial.IntVec2D;

import java.util.Random;

public class Program
{

    public static class ExamplePassability   implements IPassability
    {
        private String map = "\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0111111111111111111111111111111111111110\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0001000000000000000100000000000000000000\r\n" +
        "                    0001000000000000000100000000000000000000\r\n" +
        "                    0001000000000000000100000000000000000000\r\n" +
        "                    0001000000000000000100000000000000000000\r\n" +
        "                    0001000000000000000100000000000000000000\r\n" +
        "                    0001000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0111111111111111111100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0000000000000000000100000000000000000000\r\n" +
        "                    0111111111111111111111111111111111111110\r\n" +
        "                    0000000000000000000000000000000000000000\r\n" +
        "                ";
        public ExamplePassability() {
            obstacles = new boolean[40][40];
            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ charlines = map.Split('\n').Select(/* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(line) => {
                return line.Trim();
            }" */).Where(/* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(line) => {
                return !String.IsNullOrWhiteSpace(line);
            }" */).Select(/* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(line) => {
                return line.ToCharArray();
            }" */).ToArray();
            for (int j = 0;j < obstacles.GetLength(1);j++)
                for (int i = 0;i < obstacles.GetLength(0);i++)
                {
                    obstacles[i][j] = charlines[i][j] == '1';
                }
        }

        Random rnd = new Random(700);
        public IntVec2D getRandomFreePosition() {
            int x = rnd.nextInt(40);
            int y = rnd.nextInt(40);
            while (obstacles[x][y])
            {
                x = rnd.nextInt(40);
                y = rnd.nextInt(40);
            }
            return new IntVec2D(x, y);
        }

        private boolean[][] obstacles = new boolean[][];
        public boolean canEnter(IntVec2D pos, RefSupport<Integer> cost) {
            cost.setValue(Constants.COST_ONE);
            return !obstacles[pos.y][pos.x];
        }

    }

    //private static readonly int Height = 16;
    //private static readonly int Width = 16;
    //private static readonly Position StartPosition = new Position(1, 0);
    //private static readonly Position EndPosition = new Position(15, 15);
    public static void main6(String[] args) {
        ;
        ;
        ;
        ;
        Position startPosition = new Position(1,0);
        Position endPosition = new Position(69,69);
        // Prepare the abstract graph beforehand
        IPassability passability = new FakePassability(width,height);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ concreteMap = ConcreteMapFactory.CreateConcreteMap(width, height, passability);
        HierarchicalMapFactory abstractMapFactory = new HierarchicalMapFactory();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ absTiling = abstractMapFactory.CreateHierarchicalMap(concreteMap, clusterSize, maxLevel, EntranceStyle.EndEntrance);
        //var edges = absTiling.AbstractGraph.Nodes.SelectMany(x => x.Edges.Values)
        //    .GroupBy(x => x.Info.Level)
        //    .ToDictionary(x => x.Key, x => x.Count());
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ watch = Stopwatch.StartNew();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ regularSearchPath = RegularSearch(concreteMap, startPosition, endPosition);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ regularSearchTime = watch.ElapsedMilliseconds;
        watch = Stopwatch.StartNew();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ hierarchicalSearchPath = HierarchicalSearch(absTiling, maxLevel, concreteMap, startPosition, endPosition);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ hierarchicalSearchTime = watch.ElapsedMilliseconds;
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ pospath = hierarchicalSearchPath.Select(/* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(p) => {
            if (p instanceof ConcretePathNode)
            {
                ConcretePathNode concretePathNode = (ConcretePathNode)p;
                return concreteMap.Graph.GetNodeInfo(concretePathNode.Id).Position;
            }

            AbstractPathNode abstractPathNode = (AbstractPathNode)p;
            return absTiling.AbstractGraph.GetNodeInfo(abstractPathNode.Id).Position;
        }" */).ToList();
        Console.WriteLine("Regular search: " + regularSearchTime + " ms");
        Console.WriteLine("Number of nodes: " + regularSearchPath.Count);
        Console.WriteLine("Hierachical search: " + hierarchicalSearchTime + " ms");
        Console.WriteLine("Number of nodes: " + hierarchicalSearchPath.Count);
    }

    public static void main2(String[] args) {
        ;
        ;
        ;
        ;
        Position startPosition = new Position(17,38);
        Position endPosition = new Position(16,18);
        // Prepare the abstract graph beforehand
        IPassability passability = new FakePassability(width,height);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ concreteMap = ConcreteMapFactory.CreateConcreteMap(width, height, passability);
        HierarchicalMapFactory abstractMapFactory = new HierarchicalMapFactory();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ absTiling = abstractMapFactory.CreateHierarchicalMap(concreteMap, clusterSize, maxLevel, EntranceStyle.EndEntrance);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ watch = Stopwatch.StartNew();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ regularSearchPath = RegularSearch(concreteMap, startPosition, endPosition);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ regularSearchTime = watch.ElapsedMilliseconds;
        watch = Stopwatch.StartNew();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ hierarchicalSearchPath = HierarchicalSearch(absTiling, maxLevel, concreteMap, startPosition, endPosition);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ hierarchicalSearchTime = watch.ElapsedMilliseconds;
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ pospath = hierarchicalSearchPath.Select(/* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(p) => {
            if (p instanceof ConcretePathNode)
            {
                ConcretePathNode concretePathNode = (ConcretePathNode)p;
                return concreteMap.Graph.GetNodeInfo(concretePathNode.Id).Position;
            }

            AbstractPathNode abstractPathNode = (AbstractPathNode)p;
            return absTiling.AbstractGraph.GetNodeInfo(abstractPathNode.Id).Position;
        }" */).ToList();
        Console.WriteLine("Regular search: " + regularSearchTime + " ms");
        Console.WriteLine("Number of nodes: " + regularSearchPath.Count);
        Console.WriteLine("Hierachical search: " + hierarchicalSearchTime + " ms");
        Console.WriteLine("Number of nodes: " + hierarchicalSearchPath.Count);
    }

    public static void main(String[] args) {
        Program.Main(args);
    }

    public static void Main(String[] args) {
        ;
        ;
        ;
        ;
        //IPassability passability = new ExamplePassability();
        IPassability passability = new FakePassability(width,height);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ concreteMap = ConcreteMapFactory.CreateConcreteMap(width, height, passability);
        HierarchicalMapFactory abstractMapFactory = new HierarchicalMapFactory();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ absTiling = abstractMapFactory.CreateHierarchicalMap(concreteMap, clusterSize, maxLevel, EntranceStyle.EndEntrance);
        //var edges = absTiling.AbstractGraph.Nodes.SelectMany(x => x.Edges.Values)
        //    .GroupBy(x => x.Info.Level)
        //    .ToDictionary(x => x.Key, x => x.Count());
        Func<Position, Position, List<IPathNode>> doHierarchicalSearch = /* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(startPosition, endPosition) => {
            return HierarchicalSearch(absTiling, maxLevel, concreteMap, startPosition, endPosition);
        }" */;
        Func<Position, Position, List<IPathNode>> doRegularSearch = /* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(startPosition, endPosition) => {
            return RegularSearch(concreteMap, startPosition, endPosition);
        }" */;
        Func<List<IPathNode>, List<Position>> toPositionPath = /* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(path) => {
            return path.Select(/* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(p) => {
                if (p instanceof ConcretePathNode)
                {
                    ConcretePathNode concretePathNode = (ConcretePathNode)p;
                    return concreteMap.Graph.GetNodeInfo(concretePathNode.Id).Position;
                }

                AbstractPathNode abstractPathNode = (AbstractPathNode)p;
                return absTiling.AbstractGraph.GetNodeInfo(abstractPathNode.Id).Position;
            }" */).ToList();
        }" */;
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ points = Enumerable.Range(0, 2000).Select(/* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(_) => {
            //Position startPosition2 = new Position(18, 0);
            //Position endPosition2 = new Position(20, 0);
            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ pos1 = ((FakePassability)passability).getRandomFreePosition();
            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ pos2 = ((FakePassability)passability).getRandomFreePosition();
            while (Math.Abs(pos1.X - pos2.X) + Math.Abs(pos1.Y - pos2.Y) < 10)
            {
                pos2 = ((FakePassability)passability).getRandomFreePosition();
            }
            return Tuple.Create(pos1, pos2);
        }" */).ToArray();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ searchStrategies;
        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ searchStrategy : searchStrategies)
        {
            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ watch = Stopwatch.StartNew();
            for (int i = 0;i < points.Length;i++)
            {
                Position startPosition2 = points[i].Item1;
                Position endPosition2 = points[i].Item2;
                /* [UNSUPPORTED] 'var' as type is unsupported "var" */ regularSearchPath = searchStrategy(startPosition2, endPosition2);
                /* [UNSUPPORTED] 'var' as type is unsupported "var" */ posPath1 = toPositionPath(regularSearchPath);
            }
            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ regularSearchTime = watch.ElapsedMilliseconds;
            Console.WriteLine(regularSearchTime);
        }
    }

    private static List<IPathNode> hierarchicalSearch(HierarchicalMap hierarchicalMap, int maxLevel, ConcreteMap concreteMap, Position startPosition, Position endPosition) {
        HierarchicalMapFactory factory = new HierarchicalMapFactory();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ startAbsNode = factory.InsertAbstractNode(hierarchicalMap, startPosition);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ targetAbsNode = factory.InsertAbstractNode(hierarchicalMap, endPosition);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ maxPathsToRefine = int.MaxValue;
        HierarchicalSearch hierarchicalSearch = new HierarchicalSearch();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ abstractPath = hierarchicalSearch.DoHierarchicalSearch(hierarchicalMap, startAbsNode, targetAbsNode, maxLevel, maxPathsToRefine);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ path = hierarchicalSearch.AbstractPathToLowLevelPath(hierarchicalMap, abstractPath, hierarchicalMap.getWidth(), maxPathsToRefine);
        SmoothWizard smoother = new SmoothWizard(concreteMap, path);
        path = smoother.smoothPath();
        factory.RemoveAbstractNode(hierarchicalMap, targetAbsNode);
        factory.RemoveAbstractNode(hierarchicalMap, startAbsNode);
        return path;
    }

    private static List<IPathNode> regularSearch(ConcreteMap concreteMap, Position startPosition, Position endPosition) {
        ConcreteGraph tilingGraph = concreteMap.getGraph();
        Func<int, int, ConcreteNode> getNode = /* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(top, left) => {
            return tilingGraph.GetNode(concreteMap.GetNodeIdFromPos(top, left));
        }" */;
        // Regular pathfinding
        AStar<ConcreteNode> searcher = new AStar<ConcreteNode>(concreteMap, getNode(startPosition.X, startPosition.Y).NodeId, getNode(endPosition.X, endPosition.Y).NodeId);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ path = searcher.findPath();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ path2 = path.PathNodes;
        return new List<IPathNode>(path2.Select(/* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(p) => {
            return (IPathNode)new ConcretePathNode(p);
        }" */));
    }

    private static List<char> getCharVector(ConcreteMap concreteMap) {
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ result = new List<char>();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ numberNodes = concreteMap.getNrNodes();
        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ i = 0;i < numberNodes;i++)
        {
            result.Add(concreteMap.getGraph().GetNodeInfo(Id<ConcreteNode>.From(i)).IsObstacle ? '@' : '.');
        }
        return result;
    }

    public static void printFormatted(ConcreteMap concreteMap, HierarchicalMap hierarchicalGraph, int clusterSize, List<Position> path) {
        PrintFormatted(getCharVector(concreteMap), concreteMap, hierarchicalGraph, clusterSize, path);
    }

    private static void printFormatted(List<char> chars, ConcreteMap concreteMap, HierarchicalMap hierarchicalGraph, int clusterSize, List<Position> path) {
        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ y = 0;y < concreteMap.Height;y++)
        {
            if (y % clusterSize == 0)
                Console.WriteLine("---------------------------------------------------------");

            for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ x = 0;x < concreteMap.getWidth();x++)
            {
                Console.ForegroundColor = ConsoleColor.White;
                if (x % clusterSize == 0)
                    Console.Write('|');

                /* [UNSUPPORTED] 'var' as type is unsupported "var" */ nodeId = concreteMap.GetNodeIdFromPos(x, y);
                /* [UNSUPPORTED] 'var' as type is unsupported "var" */ hasAbsNode = hierarchicalGraph.getAbstractGraph().getNodes().SingleOrDefault(/* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(n) => {
                    return n.Info.ConcreteNodeId == nodeId;
                }" */);
                if (hasAbsNode != null)
                {
                    Level __dummyScrutVar0 = hasAbsNode.Info.Level;
                    if (__dummyScrutVar0.equals(1))
                    {
                        Console.ForegroundColor = ConsoleColor.Red;
                    }
                    else if (__dummyScrutVar0.equals(2))
                    {
                        Console.ForegroundColor = ConsoleColor.DarkGreen;
                    }

                }

                Console.Write(path.Any(/* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(node) => {
                    return node.X == x && node.Y == y;
                }" */) ? 'X' : chars[nodeId.IdValue]);
            }
            Console.WriteLine();
        }
    }

}
