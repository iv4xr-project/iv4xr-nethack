package agent.navigation;

import agent.navigation.hpastar.*;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.*;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.*;
import java.util.stream.Collectors;
import nethack.enums.Color;
import nethack.object.Level;

public class NetHackSurface extends GridSurface {
  public NetHackSurface() {
    super(Level.SIZE, 8);
  }

  @Override
  public Iterable<Tile> neighbours(Tile t) {
    List<Tile> neighbours = (List<Tile>) super.neighbours(t);

    if (getTile(t.pos) instanceof Door) {
      return neighbours.stream()
          .filter(tile -> !NavUtils.isDiagonal(t.pos, tile.pos))
          .collect(Collectors.toList());
    } else {
      return neighbours.stream()
          .filter(
              tile -> !(NavUtils.isDiagonal(t.pos, tile.pos) && getTile(tile.pos) instanceof Door))
          .collect(Collectors.toList());
    }
  }

  public boolean canBeDoor(IntVec2D pos) {
    Tile t = getTile(pos);
    if (t == null) {
      return false;
    }
    List<IntVec2D> neighbours = NavUtils.neighbourCoordinates(pos, Level.SIZE, false);
    int horizontalWalls = 0, verticalWalls = 0;
    for (IntVec2D neighbour : neighbours) {
      Tile neighbourTile = getTile(neighbour);
      boolean tileCanBeWall = neighbourTile == null || neighbourTile instanceof Wall;
      if (!tileCanBeWall) {
        continue;
      }
      if (neighbour.x == pos.x) {
        horizontalWalls++;
      } else {
        verticalWalls++;
      }
    }

    return verticalWalls + horizontalWalls == 2 && (verticalWalls == 2 || horizontalWalls == 2);
  }

  public List<IntVec2D> VisibleCoordinates(IntVec2D agentPosition, Level level) {
    resetVisibility();

    // Perform BFS on the graph, initiate the queue with the agent position and all the lit floor
    // tiles
    List<IntVec2D> agentNeighbours = NavUtils.neighbourCoordinates(agentPosition, Level.SIZE, true);
    for (IntVec2D neighbour : agentNeighbours) {
      Tile neighbourTile = getTile(neighbour);
      if (!(neighbourTile instanceof Viewable)) {
        continue;
      }
      ((Viewable) neighbourTile).setVisible(true);
    }

    HashSet<IntVec2D> visibleCoordinates = new HashSet<>(agentNeighbours);
    HashSet<IntVec2D> processedCoordinates = new HashSet<>();
    Queue<IntVec2D> queue = new LinkedList<>(level.visibleFloors);

    processedCoordinates.add(agentPosition);
    queue.addAll(NavUtils.neighbourCoordinates(agentPosition, Level.SIZE, true));

    // While there are coordinates left to be explored
    while (!queue.isEmpty()) {
      IntVec2D nextPos = queue.remove();
      // Already processed
      if (processedCoordinates.contains(nextPos)) {
        continue;
      }
      processedCoordinates.add(nextPos);

      Tile t = getTile(nextPos);
      if (!(t instanceof Viewable)) {
        continue;
      } else if (level.getEntity(nextPos).color == Color.TRANSPARENT) {
        continue;
      }

      // Get the neighbours
      List<IntVec2D> neighbours = NavUtils.neighbourCoordinates(nextPos, Level.SIZE, true);
      if (t instanceof Doorway) {
        // Does not have a lit floor tile next to it, so we assume we cannot see it
        if (neighbours.stream()
            .noneMatch(
                coord ->
                    getTile(coord) instanceof Floor
                        && level.getEntity(coord).color != Color.TRANSPARENT)) {
          continue;
        }
      }

      // Current tile is visible
      ((Viewable) t).setVisible(true);
      visibleCoordinates.add(nextPos);

      // Only add all neighbours if it is floor
      if (t instanceof Floor) {
        queue.addAll(neighbours);
      }
    }

    return new ArrayList<>(visibleCoordinates);
  }

  private void resetVisibility() {
    // First reset visibility of all tiles to false
    for (Tile[] row : tiles) {
      for (Tile t : row) {
        if (t instanceof Viewable) {
          ((Viewable) t).setVisible(false);
        }
      }
    }
  }
}
