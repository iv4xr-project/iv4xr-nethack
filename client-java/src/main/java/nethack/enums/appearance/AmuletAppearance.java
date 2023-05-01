package nethack.enums.appearance;

import java.util.HashMap;
import java.util.Map;

public enum AmuletAppearance {
  CIRCULAR("circular"),
  SPHERICAL("spherical"),
  OVAL("oval"),
  TRIANGULAR("triangular"),
  PYRAMIDAL("pyramidal"),
  SQUARE("square"),
  CONCAVE("concave"),
  HEXAGONAL("hexagonal"),
  OCTAGONAL("octagonal");

  private final String value;

  AmuletAppearance(String appearanceString) {
    value = appearanceString;
  }

  public static final Map<String, AmuletAppearance> mapping = new HashMap<>();

  static {
    for (AmuletAppearance ra : values()) {
      mapping.put(ra.value, ra);
    }
  }
}
