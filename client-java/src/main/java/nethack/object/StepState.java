package nethack.object;

import agent.navigation.surface.Tile;
import java.util.List;

public class StepState {
  public Stats stats;
  public Player player;
  public Entity[][] entities;
  public Tile[][] tiles;
  public List<Monster> monsters;
  public String message;
  public boolean done;
  public Object info;
}
