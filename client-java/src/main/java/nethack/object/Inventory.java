package nethack.object;

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
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("Inventory:%n"));
    for (Item i : items) {
      if (i == null) {
        continue;
      }
      sb.append(i);
      sb.append(System.lineSeparator());
    }
    return sb.toString();
  }
}
