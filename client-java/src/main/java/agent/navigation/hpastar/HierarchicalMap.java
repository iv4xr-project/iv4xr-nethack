//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp;

import HPASharp.AbsType;
import HPASharp.Cluster;
import HPASharp.ConcreteMap;
import HPASharp.Connection;
import HPASharp.EntrancePoint;
import HPASharp.Graph.AbstractEdge;
import HPASharp.Graph.AbstractEdgeInfo;
import HPASharp.Graph.AbstractGraph;
import HPASharp.Graph.AbstractNode;
import HPASharp.Graph.AbstractNodeInfo;
import HPASharp.Graph.ConcreteNode;
import HPASharp.Infrastructure.Constants;
import HPASharp.Infrastructure.Id;
import HPASharp.Infrastructure.IMap;
import HPASharp.Position;
import HPASharp.TileType;

/**
* Abstract maps represent, as the name implies, an abstraction
* built over the concrete map.
*/
public class HierarchicalMap   implements IMap<AbstractNode>
{
    private int __Height = new int();
    public int getHeight() {
        return __Height;
    }

    public void setHeight(int value) {
        __Height = value;
    }

    private int __Width = new int();
    public int getWidth() {
        return __Width;
    }

    public void setWidth(int value) {
        __Width = value;
    }

    private AbstractGraph __AbstractGraph;
    public AbstractGraph getAbstractGraph() {
        return __AbstractGraph;
    }

    public void setAbstractGraph(AbstractGraph value) {
        __AbstractGraph = value;
    }

    private int __ClusterSize = new int();
    public int getClusterSize() {
        return __ClusterSize;
    }

    public void setClusterSize(int value) {
        __ClusterSize = value;
    }

    private int __MaxLevel = new int();
    public int getMaxLevel() {
        return __MaxLevel;
    }

    public void setMaxLevel(int value) {
        __MaxLevel = value;
    }

    private List<Cluster> __Clusters = new List<Cluster>();
    public List<Cluster> getClusters() {
        return __Clusters;
    }

    public void setClusters(List<Cluster> value) {
        __Clusters = value;
    }

    public int getNrNodes() throws Exception {
        return getAbstractGraph().getNodes().Count;
    }

    // This list, indexed by a node id from the low level,
    // indicates to which abstract node id it maps. It is a sparse
    // array for quick access. For saving memory space, this could be implemented as a dictionary
    // NOTE: It is currently just used for insert and remove STAL
    private Dictionary<Id<ConcreteNode>, Id<AbstractNode>> __ConcreteNodeIdToAbstractNodeIdMap = new Dictionary<Id<ConcreteNode>, Id<AbstractNode>>();
    public Dictionary<Id<ConcreteNode>, Id<AbstractNode>> getConcreteNodeIdToAbstractNodeIdMap() {
        return __ConcreteNodeIdToAbstractNodeIdMap;
    }

    public void setConcreteNodeIdToAbstractNodeIdMap(Dictionary<Id<ConcreteNode>, Id<AbstractNode>> value) {
        __ConcreteNodeIdToAbstractNodeIdMap = value;
    }

    private AbsType __Type = AbsType.ABSTRACT_TILE;
    public AbsType getType() {
        return __Type;
    }

    public void setType(AbsType value) {
        __Type = value;
    }

    private int currentLevel = new int();
    private int currentClusterY0 = new int();
    private int currentClusterY1 = new int();
    private int currentClusterX0 = new int();
    private int currentClusterX1 = new int();
    public void setType(TileType tileType) throws Exception {
        switch(tileType)
        {
            case Tile:
                setType(AbsType.ABSTRACT_TILE);
                break;
            case Octile:
                setType(AbsType.ABSTRACT_OCTILE);
                break;
            case OctileUnicost:
                setType(AbsType.ABSTRACT_OCTILE_UNICOST);
                break;

        }
    }

    public HierarchicalMap(ConcreteMap concreteMap, int clusterSize, int maxLevel) throws Exception {
        setClusterSize(clusterSize);
        setMaxLevel(maxLevel);
        setType(concreteMap.getTileType());
        this.setHeight(concreteMap.Height);
        this.setWidth(concreteMap.getWidth());
        setConcreteNodeIdToAbstractNodeIdMap(new Dictionary<Id<ConcreteNode>, Id<AbstractNode>>());
        setClusters(new List<Cluster>());
        setAbstractGraph(new AbstractGraph());
    }

    public int getHeuristic(Id<AbstractNode> startNodeId, Id<AbstractNode> targetNodeId) throws Exception {
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ startPos = getAbstractGraph().getNodeInfo(startNodeId).getPosition();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ targetPos = getAbstractGraph().getNodeInfo(targetNodeId).getPosition();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ diffY = Math.Abs(startPos.Y - targetPos.Y);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ diffX = Math.Abs(startPos.X - targetPos.X);
        return (diffY + diffX) * Constants.COST_ONE;
    }

    // Manhattan distance, after testing a bit for hierarchical searches we do not need
    // the level of precision of Diagonal distance or euclidean distance
    public Cluster findClusterForPosition(Position pos) throws Exception {
        Cluster foundCluster = null;
        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ cluster : getClusters())
        {
            if (cluster.Origin.Y <= pos.Y && pos.Y < cluster.Origin.Y + cluster.Size.Height && cluster.Origin.X <= pos.X && pos.X < cluster.Origin.X + cluster.Size.Width)
            {
                foundCluster = cluster;
                break;
            }

        }
        return foundCluster;
    }

    public void addEdge(Id<AbstractNode> sourceNodeId, Id<AbstractNode> destNodeId, int cost, int level, boolean inter, List<Id<AbstractNode>> pathPathNodes) throws Exception {
        AbstractEdgeInfo edgeInfo = new AbstractEdgeInfo(cost,level,inter);
        edgeInfo.setInnerLowerLevelPath(pathPathNodes);
        getAbstractGraph().addEdge(sourceNodeId,destNodeId,edgeInfo);
    }

    public List<AbstractEdge> getNodeEdges(Id<ConcreteNode> nodeId) throws Exception {
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ node = getAbstractGraph().GetNode(getConcreteNodeIdToAbstractNodeIdMap()[nodeId]);
        return node.Edges.Values.ToList();
    }

    public Cluster getCluster(Id<Cluster> id) throws Exception {
        return getClusters()[id.getIdValue()];
    }

    /**
    * Gets the neighbours(successors) of the nodeId for the level set in the currentLevel
    */
    public IEnumerable<Connection<AbstractNode>> getConnections(Id<AbstractNode> nodeId) throws Exception {
        AbstractNode node = getAbstractGraph().getNode(nodeId);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ edges = node.getEdges();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ result = new List<Connection<AbstractNode>>();
        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ edge : edges.Values)
        {
            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ edgeInfo = edge.Info;
            if (!IsValidEdgeForLevel(edgeInfo, currentLevel))
                continue;

            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ targetNodeId = edge.TargetNodeId;
            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ targetNodeInfo = getAbstractGraph().GetNodeInfo(targetNodeId);
            if (!PositionInCurrentCluster(targetNodeInfo.Position))
                continue;

            result.Add(new Connection<AbstractNode>(targetNodeId, edgeInfo.Cost));
        }
        return result;
    }

    public void removeAbstractNode(Id<AbstractNode> abstractNodeId) throws Exception {
        AbstractNodeInfo abstractNodeInfo = getAbstractGraph().getNodeInfo(abstractNodeId);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ cluster = getClusters()[abstractNodeInfo.getClusterId().getIdValue()];
        cluster.RemoveLastEntranceRecord();
        getConcreteNodeIdToAbstractNodeIdMap().Remove(abstractNodeInfo.getConcreteNodeId());
        getAbstractGraph().removeEdgesFromAndToNode(abstractNodeId);
        getAbstractGraph().removeLastNode();
    }

    private static boolean isValidEdgeForLevel(AbstractEdgeInfo edgeInfo, int level) throws Exception {
        if (edgeInfo.getIsInterClusterEdge())
        {
            return edgeInfo.getLevel() >= level;
        }

        return edgeInfo.getLevel() == level;
    }

    public boolean positionInCurrentCluster(Position position) throws Exception {
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ y = position.Y;
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ x = position.X;
        return y >= currentClusterY0 && y <= currentClusterY1 && x >= currentClusterX0 && x <= currentClusterX1;
    }

    // Define the offset between two clusters in this level (each level doubles the cluster size)
    private int getOffset(int level) throws Exception {
        return getClusterSize() * (1 << (level - 1));
    }

    public void setAllMapAsCurrentCluster() throws Exception {
        currentClusterY0 = 0;
        currentClusterY1 = getHeight() - 1;
        currentClusterX0 = 0;
        currentClusterX1 = getWidth() - 1;
    }

    public void setCurrentClusterByPositionAndLevel(Position pos, int level) throws Exception {
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ offset = getOffset(level);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ nodeY = pos.Y;
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ nodeX = pos.X;
        currentClusterY0 = nodeY - (nodeY % offset);
        currentClusterY1 = Math.Min(this.getHeight() - 1, this.currentClusterY0 + offset - 1);
        currentClusterX0 = nodeX - (nodeX % offset);
        currentClusterX1 = Math.Min(this.getWidth() - 1, this.currentClusterX0 + offset - 1);
    }

    public boolean belongToSameCluster(Id<AbstractNode> node1Id, Id<AbstractNode> node2Id, int level) throws Exception {
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ node1Pos = getAbstractGraph().getNodeInfo(node1Id).getPosition();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ node2Pos = getAbstractGraph().getNodeInfo(node2Id).getPosition();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ offset = getOffset(level);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ currentRow1 = node1Pos.Y - (node1Pos.Y % offset);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ currentRow2 = node2Pos.Y - (node2Pos.Y % offset);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ currentCol1 = node1Pos.X - (node1Pos.X % offset);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ currentCol2 = node2Pos.X - (node2Pos.X % offset);
        if (currentRow1 != currentRow2)
            return false;

        if (currentCol1 != currentCol2)
            return false;

        return true;
    }

    public void setCurrentLevelForSearches(int level) throws Exception {
        currentLevel = level;
    }

    private boolean isValidAbstractNodeForLevel(Id<AbstractNode> abstractNodeId, int level) throws Exception {
        return getAbstractGraph().getNodeInfo(abstractNodeId).getLevel() >= level;
    }

    private int getEntrancePointLevel(EntrancePoint entrancePoint) throws Exception {
        return getAbstractGraph().getNodeInfo(entrancePoint.getAbstractNodeId()).getLevel();
    }

    public void createHierarchicalEdges() throws Exception {
        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ level = 2;level <= getMaxLevel();level++)
        {
            // Starting from level 2 denotes a serious mess on design, because lvl 1 is
            // used by the clusters.
            SetCurrentLevelForSearches(level - 1);
            int n = 1 << (level - 1);
            // Group clusters by their level. Each subsequent level doubles the amount of clusters in each group
            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ clusterGroups = getClusters().GroupBy(/* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(cl) => {
                return "{cl.ClusterX / n}_{cl.ClusterY / n}";
            }" */);
            for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ clusterGroup : clusterGroups)
            {
                /* [UNSUPPORTED] 'var' as type is unsupported "var" */ entrancesInClusterGroup = clusterGroup.SelectMany(/* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(cl) => {
                    return cl.EntrancePoints;
                }" */).Where(/* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(entrance) => {
                    return GetEntrancePointLevel(entrance) >= level;
                }" */).ToList();
                /* [UNSUPPORTED] 'var' as type is unsupported "var" */ firstEntrance = entrancesInClusterGroup.FirstOrDefault();
                if (firstEntrance == null)
                    continue;

                /* [UNSUPPORTED] 'var' as type is unsupported "var" */ entrancePosition = getAbstractGraph().GetNode(firstEntrance.AbstractNodeId).Info.Position;
                SetCurrentClusterByPositionAndLevel(entrancePosition, level);
                for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ entrance1 : entrancesInClusterGroup)
                    for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ entrance2 : entrancesInClusterGroup)
                    {
                        if (entrance1 == entrance2 || !IsValidAbstractNodeForLevel(entrance1.AbstractNodeId, level) || !IsValidAbstractNodeForLevel(entrance2.AbstractNodeId, level))
                            continue;

                        AddEdgesBetweenAbstractNodes(entrance1.AbstractNodeId, entrance2.AbstractNodeId, level);
                    }
            }
        }
    }

    public void addEdgesBetweenAbstractNodes(Id<AbstractNode> srcAbstractNodeId, Id<AbstractNode> destAbstractNodeId, int level) throws Exception {
        AStar<AbstractNode> search = new AStar<AbstractNode>(this,srcAbstractNodeId,destAbstractNodeId);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ path = search.findPath();
        if (path.PathCost >= 0)
        {
            AddEdge(srcAbstractNodeId, destAbstractNodeId, path.PathCost, level, false, new List<Id<AbstractNode>>(path.PathNodes));
            path.PathNodes.Reverse();
            AddEdge(destAbstractNodeId, srcAbstractNodeId, path.PathCost, level, false, path.PathNodes);
        }

    }

    public void addEdgesToOtherEntrancesInCluster(AbstractNodeInfo abstractNodeInfo, int level) throws Exception {
        setCurrentLevelForSearches(level - 1);
        SetCurrentClusterByPositionAndLevel(abstractNodeInfo.getPosition(), level);
        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ cluster : getClusters())
        {
            if (cluster.Origin.X >= currentClusterX0 && cluster.Origin.X <= currentClusterX1 && cluster.Origin.Y >= currentClusterY0 && cluster.Origin.Y <= currentClusterY1)
            {
                for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ entrance : cluster.EntrancePoints)
                {
                    if (abstractNodeInfo.getId() == entrance.AbstractNodeId || !IsValidAbstractNodeForLevel(entrance.AbstractNodeId, level))
                        continue;

                    AddEdgesBetweenAbstractNodes(abstractNodeInfo.getId(), entrance.AbstractNodeId, level);
                }
            }

        }
    }

    public void addHierarchicalEdgesForAbstractNode(Id<AbstractNode> abstractNodeId) throws Exception {
        AbstractNodeInfo abstractNodeInfo = getAbstractGraph().getNodeInfo(abstractNodeId);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ oldLevel = abstractNodeInfo.getLevel();
        abstractNodeInfo.setLevel(getMaxLevel());
        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ level = oldLevel + 1;level <= getMaxLevel();level++)
        {
            AddEdgesToOtherEntrancesInCluster(abstractNodeInfo, level);
        }
    }

}
