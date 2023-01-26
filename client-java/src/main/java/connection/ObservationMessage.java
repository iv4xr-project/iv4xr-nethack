package connection;

import nethack.object.*;

public class ObservationMessage {
  public Stats stats;
  public Player player;
  public final Entity[][] entities = new Entity[Level.HEIGHT][Level.WIDTH];
  public String message;
  public Item[] items;

  public ObservationMessage() {}
}
