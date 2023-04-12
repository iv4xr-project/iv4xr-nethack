//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar.infrastructure;

import org.jetbrains.annotations.NotNull;

public class Id<T> implements IId, Comparable<Id<T>> {
  public int getIdValue() {
    return _value;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Id && _value == (((Id<?>) obj)._value);
  }

  public int hashCode() {
    return Integer.valueOf(_value).hashCode();
  }

  private int _value;

  public Id() {}

  private Id(int value) {
    _value = value;
  }

  public int __cast(Id<T> id) {
    return id._value;
  }

  public Id<T> from(int value) {
    return new Id<T>(value);
  }

  public String toString() {
    return String.valueOf(_value);
  }

  @Override
  public int compareTo(@NotNull Id<T> o) {
    return Integer.compare(_value, o.getIdValue());
  }
}
