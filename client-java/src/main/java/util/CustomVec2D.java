package util;

import eu.iv4xr.framework.spatial.Vec3;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class CustomVec2D implements Serializable {
  private static final long serialVersionUID = 1L;
  public int x;
  public int y;

  public CustomVec2D(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public CustomVec2D(Vec3 p) {
    this.x = (int) p.x;
    this.y = (int) p.y;
  }

  // Add two vectors together
  public CustomVec2D add(CustomVec2D other) {
    int newX = x + other.x;
    int newY = y + other.y;
    return new CustomVec2D(newX, newY);
  }

  public static boolean adjacent(CustomVec2D p, CustomVec2D q, boolean allowDiagonally) {
    assert !p.equals(q) : "Cannot request adjacent for equal coordinates";
    int dx = Math.abs(p.x - q.x);
    int dy = Math.abs(p.y - q.y);

    // Further than 1 away or same tile
    if (dx > 1 || dy > 1 || (dx == 0 && dy == 0)) {
      return false;
    }
    return allowDiagonally || dx == 0 || dy == 0;
  }

  public static boolean straightLine(CustomVec2D p, CustomVec2D q) {
    assert !p.equals(q) : "Cannot determine straightline from equal coordinates";
    return p.x == q.x || p.y == q.y || diagonal(p, q);
  }

  public static boolean diagonal(CustomVec2D p, CustomVec2D q) {
    assert !p.equals(q) : "Cannot request diagonal for equal coordinates";
    int dx = Math.abs(p.x - q.x);
    int dy = Math.abs(p.y - q.y);

    return dx == dy;
  }

  public boolean equals(Object o) {
    if (!(o instanceof CustomVec2D)) {
      return false;
    } else {
      CustomVec2D o_ = (CustomVec2D) o;
      return this.x == o_.x && this.y == o_.y;
    }
  }

  public int hashCode() {
    return Objects.hash(this.x, this.y);
  }

  public static int manhattan(CustomVec2D p, CustomVec2D q) {
    return Math.max(Math.abs(p.x - q.x), Math.abs(p.y - q.y));
  }

  public void sortBasedOnManhattanDistance(List<CustomVec2D> targets) {
    // Sort targets based on manhattan distance
    if (targets.size() <= 1) {
      return;
    }

    List<Integer> manhattanDistances = new ArrayList<>(targets.size());
    for (CustomVec2D vec : targets) {
      manhattanDistances.add(CustomVec2D.manhattan(this, vec));
    }
    List<Integer> indexList = new ArrayList<>(targets.size());
    for (int i = 0; i < targets.size(); i++) {
      indexList.add(i);
    }

    indexList.sort(
        new Comparator<Integer>() {
          @Override
          public int compare(Integer i1, Integer i2) {
            int distance1 = manhattanDistances.get(i1);
            int distance2 = manhattanDistances.get(i2);
            return Integer.compare(distance1, distance2);
          }
        });

    List<CustomVec2D> sortedTargets = new ArrayList<>();
    for (int index : indexList) {
      sortedTargets.add(targets.get(index));
    }
    targets = sortedTargets;
  }

  public static float distSq(CustomVec2D p, CustomVec2D q) {
    float dx = (float) (p.x - q.x);
    float dy = (float) (p.y - q.y);
    return dx * dx + dy * dy;
  }

  public static float dist(CustomVec2D p, CustomVec2D q) {
    return (float) Math.sqrt((double) distSq(p, q));
  }

  public String toString() {
    return String.format("<%d,%d>", this.x, this.y);
  }
}
