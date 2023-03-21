package agent.navigation;

import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.*;
import java.util.*;
import nethack.enums.Color;
import nethack.enums.EntityType;
import nethack.object.Entity;
import nethack.object.Level;
import util.CustomVec2D;

public class NetHackSurface extends GridSurface {
  public HashSet<CustomVec2D> visibleCoordinates = new HashSet<>();

  public NetHackSurface() {
    super(Level.SIZE, 8);
  }

  public boolean canBeDoor(CustomVec2D pos) {
    Tile t = getTile(pos);
    if (t == null) {
      return false;
    }
    List<CustomVec2D> neighbours = NavUtils.neighbourCoordinates(pos, Level.SIZE, false);
    int horizontalWalls = 0, verticalWalls = 0;
    for (CustomVec2D neighbour : neighbours) {
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

  public void updateVisibleCoordinates(CustomVec2D agentPosition, Level level) {
    resetVisibility();

    // Perform BFS on the graph, initiate the queue with the agent position and all the lit floor
    // tiles
    List<CustomVec2D> agentNeighbours =
        NavUtils.neighbourCoordinates(agentPosition, Level.SIZE, true);
    for (CustomVec2D neighbour : agentNeighbours) {
      Tile neighbourTile = getTile(neighbour);
      if (!(neighbourTile instanceof Viewable)) {
        continue;
      }
      ((Viewable) neighbourTile).setVisible(true);
    }

    HashSet<CustomVec2D> visibleCoordinates = new HashSet<>(agentNeighbours);
    HashSet<CustomVec2D> processedCoordinates = new HashSet<>();
    Queue<CustomVec2D> queue = new LinkedList<>(level.visibleFloors);

    processedCoordinates.add(agentPosition);
    queue.addAll(NavUtils.neighbourCoordinates(agentPosition, Level.SIZE, true));

    // While there are coordinates left to be explored
    while (!queue.isEmpty()) {
      CustomVec2D nextPos = queue.remove();
      // Already processed
      if (processedCoordinates.contains(nextPos)) {
        continue;
      }
      processedCoordinates.add(nextPos);

      Tile t = getTile(nextPos);
      if (!(t instanceof Viewable)) {
        continue;
      }
      Entity entity = level.getEntity(nextPos);

      if (entity.color == Color.TRANSPARENT && entity.type != EntityType.MONSTER) {
        continue;
      }

      // Get the neighbours
      List<CustomVec2D> neighbours = NavUtils.neighbourCoordinates(nextPos, Level.SIZE, true);
      if (t instanceof Doorway) {
        // Does not have a lit floor tile next to it, so we assume we cannot see it
        if (neighbours.stream()
            .noneMatch(
                pos ->
                    getTile(pos) instanceof Floor
                        && level.getEntity(pos).color != Color.TRANSPARENT)) {
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

    this.visibleCoordinates = visibleCoordinates;
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
