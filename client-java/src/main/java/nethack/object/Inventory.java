package nethack.object;

import java.io.Serializable;
import nethack.object.items.Item;
import util.ColoredStringBuilder;

public class Inventory implements Serializable {
  public static final int MAX_SIZE = 55;
  public Item[] items;

  public Inventory(Item[] items) {
    assert items.length <= Inventory.MAX_SIZE
        : String.format(
            "Inventory item length should not be larger than %d but was %d",
            MAX_SIZE, items.length);
    this.items = items;
  }

  @Override
  public String toString() {
    ColoredStringBuilder csb = new ColoredStringBuilder();
    csb.append("Inventory:").newLine();
    for (Item i : items) {
      if (i == null) {
        continue;
      }
      csb.append(i).newLine();
    }
    return csb.toString();
  }
}
