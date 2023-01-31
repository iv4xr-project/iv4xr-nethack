//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:33
//

package HPASharp;

import HPASharp.Cluster;
import HPASharp.ConcreteMap;
import HPASharp.EntrancePoint;
import HPASharp.Graph.AbstractNode;
import HPASharp.Graph.ConcreteNode;
import HPASharp.Infrastructure.Id;
import HPASharp.Position;
import HPASharp.Size;

public class Cluster
{
    private Id<Cluster> __Id;
    public Id<Cluster> getId() {
        return __Id;
    }

    public void setId(Id<Cluster> value) {
        __Id = value;
    }

    private int __ClusterY = new int();
    public int getClusterY() {
        return __ClusterY;
    }

    public void setClusterY(int value) {
        __ClusterY = value;
    }

    private int __ClusterX = new int();
    public int getClusterX() {
        return __ClusterX;
    }

    public void setClusterX(int value) {
        __ClusterX = value;
    }

    /**
    * A 2D array which represents a distance between 2 entrances.
    * This array could be represented as a Dictionary, but it's faster
    * to use an array.
    */
    private final Dictionary<Tuple<Id<AbstractNode>, Id<AbstractNode>>, int> _distances = new Dictionary<Tuple<Id<AbstractNode>, Id<AbstractNode>>, int>();
    private final Dictionary<Tuple<Id<AbstractNode>, Id<AbstractNode>>, List<Id<ConcreteNode>>> _cachedPaths = new Dictionary<Tuple<Id<AbstractNode>, Id<AbstractNode>>, List<Id<ConcreteNode>>>();
    // Tells whether a path has already been calculated for 2 node ids
    private final Dictionary<Tuple<Id<AbstractNode>, Id<AbstractNode>>, boolean> _distanceCalculated = new Dictionary<Tuple<Id<AbstractNode>, Id<AbstractNode>>, boolean>();
    private List<EntrancePoint> __EntrancePoints = new List<EntrancePoint>();
    public List<EntrancePoint> getEntrancePoints() {
        return __EntrancePoints;
    }

    public void setEntrancePoints(List<EntrancePoint> value) {
        __EntrancePoints = value;
    }

    // This concreteMap object contains the subregion of the main grid that this cluster contains.
    // Necessary to do local search to find paths and distances between local entrances
    private ConcreteMap __SubConcreteMap;
    public ConcreteMap getSubConcreteMap() {
        return __SubConcreteMap;
    }

    public void setSubConcreteMap(ConcreteMap value) {
        __SubConcreteMap = value;
    }

    private Size __Size;
    public Size getSize() {
        return __Size;
    }

    public void setSize(Size value) {
        __Size = value;
    }

    private Position __Origin = new Position();
    public Position getOrigin() {
        return __Origin;
    }

    public void setOrigin(Position value) {
        __Origin = value;
    }

    public Cluster(ConcreteMap concreteMap, Id<Cluster> id, int clusterX, int clusterY, Position origin, Size size) throws Exception {
        setSubConcreteMap(concreteMap.slice(origin.X,origin.Y,size.getWidth(),size.getHeight(),concreteMap.getPassability()));
        setId(id);
        setClusterY(clusterY);
        setClusterX(clusterX);
        setOrigin(origin);
        setSize(size);
        _distances = new Dictionary<Tuple<Id<AbstractNode>, Id<AbstractNode>>, int>();
        _cachedPaths = new Dictionary<Tuple<Id<AbstractNode>, Id<AbstractNode>>, List<Id<ConcreteNode>>>();
        _distanceCalculated = new Dictionary<Tuple<Id<AbstractNode>, Id<AbstractNode>>, boolean>();
        setEntrancePoints(new List<EntrancePoint>());
    }

    public void createIntraClusterEdges() throws Exception {
        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ point1 : getEntrancePoints())
            for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ point2 : getEntrancePoints())
                ComputePathBetweenEntrances(point1, point2);
    }

    /**
    * Gets the index of the entrance point inside this cluster
    */
    private int getEntrancePositionIndex(EntrancePoint entrancePoint) throws Exception {
        return entrancePoint.getRelativePosition().Y * getSize().getWidth() + entrancePoint.getRelativePosition().X;
    }

    private void computePathBetweenEntrances(EntrancePoint e1, EntrancePoint e2) throws Exception {
        if (e1.getAbstractNodeId() == e2.getAbstractNodeId())
            return ;

        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ tuple = Tuple.Create(e1.getAbstractNodeId(), e2.getAbstractNodeId());
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ invtuple = Tuple.Create(e2.getAbstractNodeId(), e1.getAbstractNodeId());
        if (_distanceCalculated.ContainsKey(tuple))
            return ;

        Id<Cluster> startNodeId = getId().from(getEntrancePositionIndex(e1));
        Id<Cluster> targetNodeId = getId().from(getEntrancePositionIndex(e2));
        AStar<ConcreteNode> search = new AStar<ConcreteNode>(getSubConcreteMap(), startNodeId, targetNodeId);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ path = search.findPath();
        if (path.PathCost != -1)
        {
            // Yeah, we are supposing reaching A - B is the same like reaching B - A. Which
            // depending on the game this is NOT necessarily true (e.g climbing, downstepping a mountain)
            _distances[tuple] = _distances[invtuple] = path.PathCost;
            _cachedPaths[tuple] = new List<Id<ConcreteNode>>(path.PathNodes);
            path.PathNodes.Reverse();
            _cachedPaths[invtuple] = path.PathNodes;
        }

        _distanceCalculated[tuple] = _distanceCalculated[invtuple] = true;
    }

    public void updatePathsForLocalEntrance(EntrancePoint srcEntrancePoint) throws Exception {
        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ entrancePoint : getEntrancePoints())
        {
            ComputePathBetweenEntrances(srcEntrancePoint, entrancePoint);
        }
    }

    public int getDistance(Id<AbstractNode> abstractNodeId1, Id<AbstractNode> abstractNodeId2) throws Exception {
        return _distances[Tuple.Create(abstractNodeId1, abstractNodeId2)];
    }

    public List<Id<ConcreteNode>> getPath(Id<AbstractNode> abstractNodeId1, Id<AbstractNode> abstractNodeId2) throws Exception {
        return _cachedPaths[Tuple.Create(abstractNodeId1, abstractNodeId2)];
    }

    public boolean areConnected(Id<AbstractNode> abstractNodeId1, Id<AbstractNode> abstractNodeId2) throws Exception {
        return _distances.ContainsKey(Tuple.Create(abstractNodeId1, abstractNodeId2));
    }

}
