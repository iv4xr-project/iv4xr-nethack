package nethack.object;

import eu.iv4xr.framework.spatial.IntVec2D;
import nethack.enums.Color;
import nethack.enums.EntityType;

// Source: https://www.baeldung.com/java-enum-values
// Actions listed at: /python-server/lib/nle/nle/nethack/actions.py
public class Entity {
  public final int glyph;
  public final Color color;
  public final char symbol;
  public final EntityType type;
  //  public String id = new String(100);

  public Entity(int glyph, char symbol, EntityType type, Color color) {
    this.glyph = glyph;
    this.color = color;
    this.symbol = symbol;
    this.type = type;
  }

  public boolean closedDoor() {
    if (type != EntityType.DOOR) {
      throw new IllegalCallerException(
          "Checking closed door on a non door?! Got type " + type.name());
    }

    return symbol == '+';
  }

  public String createId(IntVec2D pos) {
    return String.format("%s_%d", type.name(), glyph);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (obj instanceof Entity) {
      Entity other = (Entity) obj;
      return glyph == other.glyph
          && symbol == other.symbol
          && other.color.equals(color)
          && type == other.type;
    }
    return false;
  }
}
