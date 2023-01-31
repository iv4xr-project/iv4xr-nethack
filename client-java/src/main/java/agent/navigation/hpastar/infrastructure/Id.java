//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar.infrastructure;

public class Id<T> implements IId {
  public boolean equals(Id<T> other) {
    try {
      return _value == other._value;
    } catch (RuntimeException __dummyCatchVar0) {
      throw __dummyCatchVar0;
    } catch (Exception __dummyCatchVar0) {
      throw new RuntimeException(__dummyCatchVar0);
    }
  }

  public int getIdValue() {
    return _value;
  }

  public boolean equals(Object obj) {
    try {
      if (ReferenceEquals(null, obj)) return false;

      return obj instanceof Id && equals((Id<T>) obj);
    } catch (RuntimeException __dummyCatchVar1) {
      throw __dummyCatchVar1;
    } catch (Exception __dummyCatchVar1) {
      throw new RuntimeException(__dummyCatchVar1);
    }
  }

  public int hashCode() {
    try {
      return _value.GetHashCode();
    } catch (RuntimeException __dummyCatchVar2) {
      throw __dummyCatchVar2;
    } catch (Exception __dummyCatchVar2) {
      throw new RuntimeException(__dummyCatchVar2);
    }
  }

  private final int _value;

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
