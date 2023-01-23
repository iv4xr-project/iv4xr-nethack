package connection;

import nethack.object.Entity;
import nethack.object.Item;
import nethack.object.Player;
import nethack.object.Stats;

public class ObservationMessage {
  public Stats stats;
  public Player player;
  public Entity[][] entities;
  public String message;
  public Item[] items;

  public ObservationMessage() {}
}
