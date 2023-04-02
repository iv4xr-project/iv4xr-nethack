package util;

import eu.iv4xr.framework.spatial.Vec3;
import java.io.Serializable;
import java.util.Objects;

public class CustomVec3D implements Serializable {
  private static final long serialVersionUID = 1L;
  public CustomVec2D pos;
  public int lvl;

  public CustomVec3D(int lvl, int x, int y) {
    this.pos = new CustomVec2D(x, y);
    this.lvl = lvl;
  }

  public CustomVec3D(int lvl, CustomVec2D pos) {
    this.pos = pos;
    this.lvl = lvl;
  }

  public CustomVec3D(Vec3 p) {
    this.pos = new CustomVec2D(p);
    this.lvl = (int) p.z;
  }

  public int x() {
    return pos.x;
  }

  public int y() {
    return pos.y;
  }

  public int lvl() {
    return lvl;
  }

  public boolean equals(Object o) {
    if (!(o instanceof CustomVec3D)) {
      return false;
    } else {
      CustomVec3D o_ = (CustomVec3D) o;
      return this.lvl == o_.lvl && pos.equals(o_.pos);
    }
  }

  public Vec3 toVec3() {
    return new Vec3(pos.x, pos.y, lvl);
  }

  public static boolean adjacent(CustomVec3D p, CustomVec3D q, boolean allowDiagonally) {
    if (p.lvl != q.lvl) {
      return false;
    }
    return CustomVec2D.adjacent(p.pos, q.pos, allowDiagonally);
  }

  public static boolean sameLvl(CustomVec3D p, CustomVec3D q) {
    return p.lvl == q.lvl;
  }

  public String toString() {
    return String.format("<%d%s>", lvl, pos);
  }

  public int hashCode() {
    return Objects.hash(lvl, pos);
  }
}
