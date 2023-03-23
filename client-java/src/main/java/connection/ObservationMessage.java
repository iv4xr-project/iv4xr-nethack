package connection;

import nethack.enums.TileType;
import nethack.object.*;
import nethack.object.items.Item;
import util.ColoredStringBuilder;

public class ObservationMessage {
  public Stats stats;
  public Player player;
  public final Entity[][] entities = new Entity[Level.SIZE.height][Level.SIZE.width];
  public final TileType[][] tileTypes = new TileType[Level.SIZE.height][Level.SIZE.width];
  public final byte[][] tileFlags = new byte[Level.SIZE.height][Level.SIZE.width];
  public String message;
  public Item[] items;

  public ObservationMessage() {}

  public String toString() {
    ColoredStringBuilder csb = new ColoredStringBuilder();
    for (int y = 0; y < Level.SIZE.height; y++) {
      for (int x = 0; x < Level.SIZE.width; x++) {
        TileType tileType = tileTypes[y][x];
        csb.append(tileType.color, tileType.symbol);
      }
      csb.append("  ");
      for (int x = 0; x < Level.SIZE.width; x++) {
        byte tileFlag = tileFlags[y][x];
        csb.append(tileFlag).append(",");
      }
      csb.newLine();
    }
    return csb.toString();
  }
}
