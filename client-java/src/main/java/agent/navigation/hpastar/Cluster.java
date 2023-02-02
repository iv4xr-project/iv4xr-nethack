//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:33
//

package agent.navigation.hpastar;

import agent.navigation.hpastar.graph.AbstractNode;
import agent.navigation.hpastar.graph.ConcreteNode;
import agent.navigation.hpastar.infrastructure.Id;
import agent.navigation.hpastar.search.AStar;
import agent.navigation.hpastar.search.Path;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.*;
import nl.uu.cs.aplib.utils.Pair;

public class Cluster {
  public Id<Cluster> id;
  public int clusterY;
  public int clusterX;

  /**
   * A 2D array which represents a distance between 2 entrances. This array could be represented as
   * a Dictionary, but it's faster to use an array.
   */
  private final Map<Pair<Id<AbstractNode>, Id<AbstractNode>>, Integer> _distances =
      new HashMap<Pair<Id<AbstractNode>, Id<AbstractNode>>, Integer>();

  private final Map<Pair<Id<AbstractNode>, Id<AbstractNode>>, List<Id<ConcreteNode>>> _cachedPaths =
      new HashMap<Pair<Id<AbstractNode>, Id<AbstractNode>>, List<Id<ConcreteNode>>>();
  // Tells whether a path has already been calculated for 2 node ids
  private final Map<Pair<Id<AbstractNode>, Id<AbstractNode>>, Boolean> _distanceCalculated =
      new HashMap<Pair<Id<AbstractNode>, Id<AbstractNode>>, Boolean>();
  ;
  public List<EntrancePoint> entrancePoints = new ArrayList<>();

  // This concreteMap object contains the subregion of the main grid that this cluster contains.
  // Necessary to do local search to find paths and distances between local entrances
  public ConcreteMap subConcreteMap;
  public Size size;
  public IntVec2D origin;

  public Cluster(
      ConcreteMap concreteMap,
      Id<Cluster> id,
      int clusterX,
      int clusterY,
      IntVec2D origin,
      Size size) {
    subConcreteMap =
        concreteMap.slice(origin.x, origin.y, size.width, size.height, concreteMap.passability);
    this.id = id;
    this.clusterY = clusterY;
    this.clusterX = clusterX;
    this.origin = origin;
    this.size = size;
  }

  public void createIntraClusterEdges() {
    for (EntrancePoint point1 : entrancePoints) {
      for (EntrancePoint point2 : entrancePoints) {
        computePathBetweenEntrances(point1, point2);
      }
    }
  }

  /** Gets the index of the entrance point inside this cluster */
  private int getEntrancePositionIndex(EntrancePoint entrancePoint) {
    return entrancePoint.relativePosition.y * size.width + entrancePoint.relativePosition.x;
  }

  private void computePathBetweenEntrances(EntrancePoint e1, EntrancePoint e2) {
    if (e1.abstractNodeId == e2.abstractNodeId) return;

    Pair<Id<AbstractNode>, Id<AbstractNode>> tuple =
        new Pair<>(e1.abstractNodeId, e2.abstractNodeId);
    Pair<Id<AbstractNode>, Id<AbstractNode>> invtuple =
        new Pair<>(e2.abstractNodeId, e1.abstractNodeId);
    if (_distanceCalculated.containsKey(tuple)) {
      return;
    }

    Id<ConcreteNode> startNodeId = new Id<ConcreteNode>().from(getEntrancePositionIndex(e1));
    Id<ConcreteNode> targetNodeId = new Id<ConcreteNode>().from(getEntrancePositionIndex(e2));
    AStar<ConcreteNode> search = new AStar<>(subConcreteMap, startNodeId, targetNodeId);
    Path<ConcreteNode> path = search.findPath();
    if (path.pathCost != -1) {
      // Yeah, we are supposing reaching A - B is the same like reaching B - A. Which
      // depending on the game this is NOT necessarily true (e.g climbing, downstepping a mountain)
      _distances.put(tuple, path.pathCost);
      _distances.put(invtuple, path.pathCost);
      _cachedPaths.put(tuple, new ArrayList<>(path.pathNodes));
      Collections.reverse(path.pathNodes);
      _cachedPaths.put(invtuple, path.pathNodes);
    }

    _distanceCalculated.put(tuple, true);
    _distanceCalculated.put(invtuple, true);
  }

  public void updatePathsForLocalEntrance(EntrancePoint srcEntrancePoint) {
    for (EntrancePoint entrancePoint : entrancePoints) {
      computePathBetweenEntrances(srcEntrancePoint, entrancePoint);
    }
  }

  public int getDistance(Id<AbstractNode> abstractNodeId1, Id<AbstractNode> abstractNodeId2) {
    return _distances.get(new Pair<>(abstractNodeId1, abstractNodeId2));
  }

  public List<Id<ConcreteNode>> getPath(
      Id<AbstractNode> abstractNodeId1, Id<AbstractNode> abstractNodeId2) {
    return _cachedPaths.get(new Pair<>(abstractNodeId1, abstractNodeId2));
  }

  public boolean areConnected(Id<AbstractNode> abstractNodeId1, Id<AbstractNode> abstractNodeId2) {
    return _distances.containsKey(new Pair<>(abstractNodeId1, abstractNodeId2));
  }

  public int numberOfEntrances() {
    return entrancePoints.size();
  }

  public EntrancePoint addEntrance(Id<AbstractNode> abstractNodeId, IntVec2D relativePosition) {
    EntrancePoint entrancePoint = new EntrancePoint(abstractNodeId, relativePosition);
    entrancePoints.add(entrancePoint);
    return entrancePoint;
  }

  public void removeLastEntranceRecord() {
    EntrancePoint entrancePoint = entrancePoints.get(entrancePoints.size() - 1);
    Id<AbstractNode> abstractNodeToRemove = entrancePoint.abstractNodeId;
    entrancePoints.remove(entrancePoints.size() - 1);
    List<Pair<Id<AbstractNode>, Id<AbstractNode>>> keysToRemove = new ArrayList<>();
    for (Pair<Id<AbstractNode>, Id<AbstractNode>> key : _distanceCalculated.keySet()) {
      if (key.fst == abstractNodeToRemove || key.snd == abstractNodeToRemove) {
        keysToRemove.add(key);
      }
    }

    for (Pair<Id<AbstractNode>, Id<AbstractNode>> key : keysToRemove) {
      _distanceCalculated.remove(key);
      _distances.remove(key);
      _cachedPaths.remove(key);
    }
  }

  @Override
  public String toString() {
    return String.format("Cluster %s (%d,%d)", id, clusterX, clusterY);
  }
}
