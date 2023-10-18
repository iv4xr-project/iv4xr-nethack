package nethack.object;

import agent.navigation.surface.Tile;
import nethack.enums.HungerState;
import org.apache.commons.lang3.NotImplementedException;
import util.CustomVec3D;

// Class to contain information
public class LTLState {
  public int score;
  public int hp;
  public int hpMax;
  public int energy;
  public int energyMax;
  public int lvl;
  public HungerState hungerState;
  public int experienceLevel;
  public int experiencePoints;
  public Turn turn;
  public Tile tile;
  public CustomVec3D loc;

  public void printLTLInformation(int ltlIndex) {
    switch (ltlIndex) {
      case 0 -> System.out.printf("score: %d%n", score);
      case 1 -> System.out.printf("hp: %d/%d%n", hp, hpMax);
      case 2 -> System.out.printf("energy: %d/%d%n", energy, energyMax);
      case 3 -> System.out.printf("levelNr: %d%n", lvl);
      case 4 -> System.out.printf("hungerState: %s%n", hungerState.toString());
      case 5 -> System.out.printf("experience: %d (%d)%n", experienceLevel, experiencePoints);
      case 6 -> System.out.printf("turn: %s%n", turn.toString());
      case 7 -> System.out.printf("tile: %s%n", tile.getClass());
      case 8 -> System.out.printf("loc: %s%n", loc.toString());
      default -> throw new NotImplementedException("There are only 9 LTLs");
    }
  }
}
