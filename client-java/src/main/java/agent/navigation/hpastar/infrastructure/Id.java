//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar.infrastructure;

public class Id<T> implements IId {
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
    try {
      return String.valueOf(_value);
    } catch (RuntimeException __dummyCatchVar3) {
      throw __dummyCatchVar3;
    } catch (Exception __dummyCatchVar3) {
      throw new RuntimeException(__dummyCatchVar3);
    }
  }
}
