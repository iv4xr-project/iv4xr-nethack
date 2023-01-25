package connection;

import nethack.object.*;

public class ObservationMessage {
  public final Stats stats = new Stats();
  public final Player player = new Player();
  public final Entity[][] entities = new Entity[Level.HEIGHT][Level.WIDTH];
  public String message;
  public Item[] items;

  public ObservationMessage() {}
}
