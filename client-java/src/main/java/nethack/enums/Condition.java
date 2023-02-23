package nethack.enums;

import java.util.HashMap;
import java.util.Map;

public enum Condition {
  STONED(0),
  SLIMED(1),
  STRANGLED(2),
  FOOD_POISONED(3),
  TERMINALLY_ILL(4),
  BLIND(5),
  DEAF(6),
  STUNNED(7),
  CONFUSED(8),
  HALLUCINATING(9),
  LEVITATING(10),
  FLYING(11),
  RIDING(12);

  private static final Map<Integer, Condition> BY_VALUE = new HashMap<>();

  static {
    for (Condition condition : values()) {
      BY_VALUE.put(condition.value, condition);
    }
  }

  final int value;

  Condition(int value) {
    this.value = value;
  }

  public static Condition fromValue(int value) {
    assert BY_VALUE.containsKey(value) : String.format("Condition value '%d' not known", value);
    return BY_VALUE.get(value);
  }
}
