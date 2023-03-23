package connection;

import nethack.enums.TileType;
import nethack.object.*;
import nethack.object.items.Item;

public class ObservationMessage {
  public Stats stats;
  public Player player;
  public final Entity[][] entities = new Entity[Level.SIZE.height][Level.SIZE.width];
  public final TileType[][] tileTypes = new TileType[Level.SIZE.height][Level.SIZE.width];
  public String message;
  public Item[] items;

  public ObservationMessage() {}
}
