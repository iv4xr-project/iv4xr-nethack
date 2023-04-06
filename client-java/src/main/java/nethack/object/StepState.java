package nethack.object;

import agent.navigation.surface.Tile;
import java.util.List;

public class StepState {
  public Stats stats;
  public Player player;
  public Symbol[][] symbols;
  public Tile[][] tiles;
  public List<Monster> monsters;
  public List<Entity> entities;
  public String message;
  public boolean done;
  public Object info;
}
