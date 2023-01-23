package nethack.enums;

import java.util.HashMap;
import java.util.Map;

public enum Alignment {
  LAWFUL(1),
  NEUTRAL(0),
  CHAOTIC(-1);

  private static final Map<Integer, Alignment> BY_VALUE = new HashMap<>();

  static {
    for (Alignment alignment : values()) {
      BY_VALUE.put(alignment.value, alignment);
    }
  }

  final int value;

  Alignment(int value) {
    this.value = value;
  }

  public static Alignment fromValue(int value) {
    if (BY_VALUE.containsKey(value)) {
      return BY_VALUE.get(value);
    }
    throw new IllegalArgumentException("Alignment value not known: " + value);
  }
}
