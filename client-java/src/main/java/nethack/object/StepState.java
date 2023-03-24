package nethack.object;

import agent.navigation.surface.Tile;

public class StepState {
  public Stats stats;
  public Player player;
  public Entity[][] entities;
  public Tile[][] tiles;
  public String message;
  public boolean done;
  public Object info;
}
