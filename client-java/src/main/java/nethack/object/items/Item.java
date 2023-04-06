package nethack.object.items;

import nethack.enums.EntityClass;

public class Item {
  // Additional information possibly at: server-python\lib\nle\build\include\onames.h
  public final char symbol;
  public final EntityClass type;
  public final int glyph;
  public final String description;
  public int quantity;

  public Item(char symbol, EntityClass type, int glyph, String description, int quantity) {
    this.symbol = symbol;
    this.type = type;
    this.description = description;
    this.glyph = glyph;
    this.quantity = quantity;
  }

  @Override
  public String toString() {
    String formatStr = "%s %-" + EntityClass.maxLength() + "s (%4d) %s";
    return String.format(formatStr, symbol, type, glyph, description);
  }
}
