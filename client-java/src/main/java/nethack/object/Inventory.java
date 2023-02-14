package nethack.object;

import nethack.util.ColoredStringBuilder;

public class Inventory {
  public static final int MAX_SIZE = 55;
  public Item[] items;

  public Inventory(Item[] items) {
    if (items.length > Inventory.MAX_SIZE) {
      throw new IllegalArgumentException(
          String.format(
              "Inventory item length should not be larger than %d but was %d",
              MAX_SIZE, items.length));
    }

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
