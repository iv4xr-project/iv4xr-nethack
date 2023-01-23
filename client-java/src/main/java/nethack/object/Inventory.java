package nethack.object;

public class Inventory {
  public static final int SIZE = 55;
  public Item[] items;

  public Inventory(Item[] items) {
    if (items.length != Inventory.SIZE) {
      throw new IllegalArgumentException(
          String.format("Inventory item length should be %d but was %d", SIZE, items.length));
    }

    this.items = items;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Item i : items) {
      if (i == null) {
        continue;
      }
      sb.append(i.toString());
      sb.append(System.lineSeparator());
    }
    return sb.toString();
  }
}
