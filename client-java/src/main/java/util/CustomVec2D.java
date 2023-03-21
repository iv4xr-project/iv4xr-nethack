package util;

import eu.iv4xr.framework.spatial.Vec3;
import java.io.Serializable;
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