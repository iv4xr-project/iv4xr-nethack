//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:33
//

package agent.navigation.hpastar;

import agent.navigation.hpastar.graph.AbstractNode;
import agent.navigation.hpastar.graph.ConcreteNode;
import agent.navigation.hpastar.infrastructure.Id;
import agent.navigation.hpastar.search.AStar;
import agent.navigation.hpastar.search.Path;
import java.util.*;
import nl.uu.cs.aplib.utils.Pair;
import util.CustomVec2D;

public class Cluster {
  public final Id<Cluster> id;
  public final int clusterY;
  public final int clusterX;

  /**
   * A 2D array which represents a distance between 2 entrances. This array could be represented as
   * a Dictionary, but it's faster to use an array.
   */
  private final Map<Pair<Id<AbstractNode>, Id<AbstractNode>>, Integer> distances = new HashMap<>();

  private final Map<Pair<Id<AbstractNode>, Id<AbstractNode>>, List<Id<ConcreteNode>>> cachedPaths =
      new HashMap<>();
  // Tells whether a path has already been calculated for 2 node ids
  private final Map<Pair<Id<AbstractNode>, Id<AbstractNode>>, Boolean> distanceCalculated =
      new HashMap<>();
  public final List<EntrancePoint> entrancePoints = new ArrayList<>();

  // This concreteMap object contains the subregion of the main grid that this cluster contains.
  // Necessary to do local search to find paths and distances between local entrances
  public final ConcreteMap subConcreteMap;
  public Size size;
  public CustomVec2D origin;

  public Cluster(
      ConcreteMap concreteMap,
      Id<Cluster> id,
      int clusterX,
      int clusterY,
      CustomVec2D origin,
      Size size) {
    subConcreteMap = concreteMap.slice(origin.x, origin.y, size, concreteMap.passability);
    this.id = id;
    this.clusterY = clusterY;
    this.clusterX = clusterX;
    assert origin.x >= 0 && origin.y >= 0 : "Cannot handle negative coordinates";
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

  public CustomVec2D toRelativePos(CustomVec2D pos) {
    assert pos.x >= origin.x
        && pos.x < origin.x + size.width
        && pos.y >= origin.y
        && pos.y < origin.y + size.height;
    return new CustomVec2D(pos.x - origin.x, pos.y - origin.y);
  }

  /** Gets the index of the entrance point inside this cluster */
  private int getEntrancePositionIndex(EntrancePoint entrancePoint) {
    return entrancePoint.relativePosition.y * size.width + entrancePoint.relativePosition.x;
  }

  private void computePathBetweenEntrances(EntrancePoint e1, EntrancePoint e2) {
    if (e1.abstractNodeId == e2.abstractNodeId) return;

    Pair<Id<AbstractNode>, Id<AbstractNode>> tuple =
        new Pair<>(e1.abstractNodeId, e2.abstractNodeId);
    Pair<Id<AbstractNode>, Id<AbstractNode>> invTuple =
        new Pair<>(e2.abstractNodeId, e1.abstractNodeId);
    if (distanceCalculated.containsKey(tuple)) {
      return;
    }

    Id<ConcreteNode> startNodeId = new Id<ConcreteNode>().from(getEntrancePositionIndex(e1));
    Id<ConcreteNode> targetNodeId = new Id<ConcreteNode>().from(getEntrancePositionIndex(e2));
    AStar<ConcreteNode> search = new AStar<>(subConcreteMap, startNodeId, targetNodeId);
    Path<ConcreteNode> path = search.findPath();
    if (path != null) {
      // Yeah, we suppose reaching A - B is the same as reaching B - A. Which
      // depending on the game this is NOT necessarily true (e.g. climbing, stepping down a
      // mountain)
      distances.put(tuple, path.pathCost);
      distances.put(invTuple, path.pathCost);
      cachedPaths.put(tuple, new ArrayList<>(path.pathNodes));
      Collections.reverse(path.pathNodes);
      cachedPaths.put(invTuple, path.pathNodes);
    }

    distanceCalculated.put(tuple, true);
    distanceCalculated.put(invTuple, true);
  }

  public void updatePathsForLocalEntrance(EntrancePoint srcEntrancePoint) {
    for (EntrancePoint entrancePoint : entrancePoints) {
      computePathBetweenEntrances(srcEntrancePoint, entrancePoint);
    }
  }

  public int getDistance(Id<AbstractNode> abstractNodeId1, Id<AbstractNode> abstractNodeId2) {
    return distances.get(new Pair<>(abstractNodeId1, abstractNodeId2));
  }

  public List<Id<ConcreteNode>> getPath(
      Id<AbstractNode> abstractNodeId1, Id<AbstractNode> abstractNodeId2) {
    return cachedPaths.get(new Pair<>(abstractNodeId1, abstractNodeId2));
  }

  public boolean areConnected(Id<AbstractNode> abstractNodeId1, Id<AbstractNode> abstractNodeId2) {
    return distances.containsKey(new Pair<>(abstractNodeId1, abstractNodeId2));
  }

  public int numberOfEntrances() {
    return entrancePoints.size();
  }

  public EntrancePoint addEntrance(Id<AbstractNode> abstractNodeId, CustomVec2D relativePosition) {
    EntrancePoint entrancePoint = new EntrancePoint(abstractNodeId, relativePosition);
    entrancePoints.add(entrancePoint);
    return entrancePoint;
  }

  public void removeRelevantEntranceRecord(Id<AbstractNode> nodeId) {
    entrancePoints.removeIf(entrancePoint -> entrancePoint.abstractNodeId.equals(nodeId));
    List<Pair<Id<AbstractNode>, Id<AbstractNode>>> keysToRemove = new ArrayList<>();
    for (Pair<Id<AbstractNode>, Id<AbstractNode>> key : distanceCalculated.keySet()) {
      if (key.fst == nodeId || key.snd == nodeId) {
        keysToRemove.add(key);
      }
    }

    for (Pair<Id<AbstractNode>, Id<AbstractNode>> key : keysToRemove) {
      distanceCalculated.remove(key);
      distances.remove(key);
      cachedPaths.remove(key);
    }
  }

  @Override
  public String toString() {
    return String.format(
        "Cluster %s (%d,%d) origin:%s size:%d,%d",
        id, clusterX, clusterY, origin, size.width, size.height);
  }
}
