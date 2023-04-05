package connection;

import agent.navigation.surface.Tile;
import java.util.List;
import nethack.object.*;
import nethack.object.items.Item;

public class ObservationMessage {
  public Stats stats;
  public Player player;
  public String message;
  public Entity[][] entities;
  public Tile[][] tiles;
  public Item[] items;
  public List<Monster> monsters;
}
