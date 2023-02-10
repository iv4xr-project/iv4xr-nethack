package nethack.enums;

import java.util.HashMap;
import java.util.Map;

public enum HungerState {
  OVERSATIATED(-1),
  SATIATED(0),
  NORMAL(1),
  HUNGRY(2),
  WEAK(3),
  FAINTING(4),
  STARVED(5);

  private static final Map<Integer, HungerState> BY_VALUE = new HashMap<>();

  static {
    for (HungerState hungerState : values()) {
      BY_VALUE.put(hungerState.value, hungerState);
    }
  }

  final int value;

  HungerState(int value) {
    this.value = value;
  }

  public static HungerState fromValue(int value) {
    assert BY_VALUE.containsKey(value) : String.format("HungerState value '%d' not known", value);
    return BY_VALUE.get(value);
  }
}
