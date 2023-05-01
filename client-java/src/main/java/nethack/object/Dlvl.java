package nethack.object;

import java.io.Serializable;
import java.util.Objects;

public class Dlvl implements Serializable {
  public final int depth;
  public final int dungeonNumber;

  public Dlvl(int depth, int dungeonNumber) {
    this.depth = depth;
    this.dungeonNumber = dungeonNumber;
  }

  public boolean equals(Object o) {
    if (!(o instanceof Dlvl other)) {
      return false;
    }

    return depth == other.depth && dungeonNumber == other.dungeonNumber;
  }

  public int hashCode() {
    return Objects.hash(depth, dungeonNumber);
  }

  public String toString() {
    return String.format("Dlvl:%d(%d)", depth, dungeonNumber);
  }
}
