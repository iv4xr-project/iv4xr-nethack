package nethack.enums;

import java.util.HashMap;
import java.util.Map;

public enum Encumbrance {
  UNENCUMBERED(0),
  BURDENED(1),
  STRESSED(2),
  STRAINED(3),
  OVERTAXED(4),
  OVERLOADED(5);
  private static final Map<Integer, Encumbrance> BY_VALUE = new HashMap<>();

  static {
    for (Encumbrance encumbrance : values()) {
      BY_VALUE.put(encumbrance.value, encumbrance);
    }
  }

  final int value;

  Encumbrance(int value) {
    this.value = value;
  }

  public static Encumbrance fromValue(int value) {
    assert BY_VALUE.containsKey(value) : String.format("Encumbrance value '%d' not known", value);
    return BY_VALUE.get(value);
  }
}
