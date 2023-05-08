package nethack.object;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import nethack.enums.Condition;
import util.ColoredStringBuilder;

public class Conditions implements Serializable {
  private final Set<Condition> conditions;

  public Conditions(Set<Condition> conditions) {
    this.conditions = conditions;
  }

  public boolean hasCondition(Condition condition) {
    return conditions.contains(condition);
  }

  public static Conditions fromValue(int conditionFlags) {
    if (conditionFlags == 0) {
      return new Conditions(new HashSet<>());
    }

    Set<Condition> conditions = new HashSet<>();
    for (int i = 0; i < Condition.values().length; i++) {
      if ((conditionFlags & (1 << i)) != 0) {
        conditions.add(Condition.fromValue(i));
      }
    }
    return new Conditions(conditions);
  }

  public String toString() {
    ColoredStringBuilder sb = new ColoredStringBuilder();
    sb.append('[');

    boolean isFirst = true;
    for (Condition condition : conditions) {
      if (!isFirst) {
        sb.append(", ");
      }
      sb.append(condition.toString());
      isFirst = false;
    }

    sb.append(']');
    return sb.toString();
  }
}
