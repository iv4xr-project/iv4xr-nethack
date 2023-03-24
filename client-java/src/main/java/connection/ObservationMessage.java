package connection;

import agent.navigation.surface.Tile;
import nethack.object.*;
import nethack.object.items.Item;
import nethack.world.Level;

public class ObservationMessage {
  public Stats stats;
  public Player player;
  public String message;
  public final Entity[][] entities = new Entity[Level.SIZE.height][Level.SIZE.width];
  public final Tile[][] tiles = new Tile[Level.SIZE.height][Level.SIZE.width];
  public Item[] items;

  public ObservationMessage() {}
}
