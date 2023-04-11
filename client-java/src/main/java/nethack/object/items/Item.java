package nethack.object.items;

import nethack.object.info.EntityInfo;

public class Item {
  // Additional information possibly at: server-python\lib\nle\build\include\onames.h
  public final char symbol;
  public final EntityInfo entityInfo;
  public final int glyph;
  public final String description;
  public int quantity;

  public Item(char symbol, EntityInfo entityInfo, int glyph, String description, int quantity) {
    this.symbol = symbol;
    this.entityInfo = entityInfo;
    this.description = description;
    this.glyph = glyph;
    this.quantity = quantity;
  }

  @Override
  public String toString() {
    String formatStr = "%s %s (%4d) %s";
    return String.format(formatStr, symbol, entityInfo, glyph, description);
  }
}
