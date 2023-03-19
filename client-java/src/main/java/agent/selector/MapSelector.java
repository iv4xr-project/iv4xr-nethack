package agent.selector;

public abstract class MapSelector {
  //    public Integer select(List<CustomVec3D> coordinates, AgentState S, Selector.SelectionType
  //   selectionType) {
  //      if (coordinates.isEmpty()) {
  //        return null;
  //      }
  //
  //      // The selection type does not matter if there is no choice
  //      int n = coordinates.size();
  //      if (n == 1 || selectionType == Selector.SelectionType.FIRST) {
  //        return 0;
  //      }
  //
  //      if (selectionType == Selector.SelectionType.LAST) {
  //        return n - 1;
  //      }
  //
  //      if (selectionType == Selector.SelectionType.CLOSEST) {
  //        return selectClosest(tiles, S);
  //      }
  //
  //      // Goes wrong for multiple levels
  //      CustomVec3D agentLoc = S.loc();
  //      float min = NetHackSurface.heuristic(agentLoc, tiles.get(0).snd.pos);
  //      float max = min;
  //      int minIndex = 0, maxIndex = 0;
  //      for (int i = 1; i < n; i++) {
  //        float dist = NetHackSurface.heuristic(agentPos, tiles.get(i).snd.pos);
  //        if (dist < min) {
  //          min = dist;
  //          minIndex = i;
  //        } else if (dist > max) {
  //          max = dist;
  //          maxIndex = i;
  //        }
  //      }
  //
  //      if (selectionType == Selector.SelectionType.FARTHEST) {
  //        return tiles.get(maxIndex);
  //      } else {
  //        throw new UnknownError("SelectionType not implemented: " + selectionType);
  //      }
  //    }
  //
  //    public int selectShortest(List<CustomVec3D> locations, AgentState S) {
  //      int n = locations.size();
  //      NetHackSurface surface = S.area();
  //      CustomVec3D agentLoc = S.loc();
  //      List<CustomVec2D> shortestPath = null;
  //      Tile closestTile = null;
  //
  //      for (CustomVec3D location : locations) {
  //        assert agentLoc.lvl == location.lvl
  //                : "The level must be the same for closest/furthest navigation";
  //
  //        // Cannot be shorter since distance is at least equal
  //        if (shortestPath != null
  //                && CustomVec2D.manhattan(agentLoc.pos, location.pos) > shortestPath.size()) {
  //          continue;
  //        }
  //
  //        S.hierarchicalNav.findPath()
  //
  //        List<CustomVec2D> path = surface.findPath(agentLoc.pos, location.pos);
  //        if (path == null) {
  //          continue;
  //        }
  //        if (path.size() == shortestPath.size()) {
  //
  //        }
  //        if (shortestPath == null || path.size() < shortestPath.size()) {
  //          shortestPath = path;
  //          if (path.isEmpty()) {
  //            closestTile = agentTile;
  //          } else {
  //            closestTile = path.get(path.size() - 1);
  //          }
  //        }
  //      }
  //
  //      if (shortestPath == null) {
  //        return null;
  //      } else if (shortestPath.isEmpty()) {
  //        return NavUtils.loc3(S.worldmodel.position);
  //      }
  //      return new Pair<>(NavUtils.levelNr(S.worldmodel.position), closestTile);
  //    }
}
