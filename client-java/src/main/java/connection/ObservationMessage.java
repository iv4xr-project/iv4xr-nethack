package connection;

import nethack.object.*;

public class ObservationMessage {
  public Stats stats;
  public Player player;
  public final Entity[][] entities = new Entity[Level.SIZE.height][Level.SIZE.width];
  public String message;
  public Item[] items;

  public ObservationMessage() {}
}
