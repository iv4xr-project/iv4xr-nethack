package nethack.object;

import nethack.enums.ItemType;

public class Item {
  // Additional information possibly at: server-python\lib\nle\build\include\onames.h
  public final char symbol;
  public final ItemType type;
  public final String description;

  public Item(char symbol, ItemType type, String description) {
    this.symbol = symbol;
    this.type = type;
    this.description = description;
  }

  @Override
  public String toString() {
    String formatStr = "%s %-" + ItemType.maxLength() + "s %s";
    return String.format(formatStr, symbol, type, description);
  }
}
