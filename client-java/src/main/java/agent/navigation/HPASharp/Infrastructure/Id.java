//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp.Infrastructure;

import HPASharp.Infrastructure.Id;
import HPASharp.Infrastructure.IId;

public class Id <T>  implements IId
{
    public Id() {
    }

    public boolean equals(Id<T> other) {
        try
        {
            return _value == other._value;
        }
        catch (RuntimeException __dummyCatchVar0)
        {
            throw __dummyCatchVar0;
        }
        catch (Exception __dummyCatchVar0)
        {
            throw new RuntimeException(__dummyCatchVar0);
        }

    }

    public int getIdValue() throws Exception {
        return _value;
    }

    public boolean equals(Object obj) {
        try
        {
            if (ReferenceEquals(null, obj))
                return false;

            return obj instanceof Id && equals((Id<T>)obj);
        }
        catch (RuntimeException __dummyCatchVar1)
        {
            throw __dummyCatchVar1;
        }
        catch (Exception __dummyCatchVar1)
        {
            throw new RuntimeException(__dummyCatchVar1);
        }

    }

    public int hashCode() {
        try
        {
            return _value.GetHashCode();
        }
        catch (RuntimeException __dummyCatchVar2)
        {
            throw __dummyCatchVar2;
        }
        catch (Exception __dummyCatchVar2)
        {
            throw new RuntimeException(__dummyCatchVar2);
        }

    }



    private final int _value = new int();
    private Id(int value) throws Exception {
        _value = value;
    }

    public static int __cast(Id<T> id) throws Exception {
        return id._value;
    }

    public static Id<T> from(int value) throws Exception {
        return new Id<T>(value);
    }

    public String toString() {
        try
        {
            return _value.ToString();
        }
        catch (RuntimeException __dummyCatchVar3)
        {
            throw __dummyCatchVar3;
        }
        catch (Exception __dummyCatchVar3)
        {
            throw new RuntimeException(__dummyCatchVar3);
        }

    }

}
