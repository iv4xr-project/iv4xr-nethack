package nethack.object;

import eu.iv4xr.framework.spatial.IntVec2D;
import eu.iv4xr.framework.spatial.Vec3;
import nethack.enums.Alignment;
import nethack.enums.HungerState;

public class Player {
  public static final String ID = "player";
  public Inventory inventory;
  public Vec3 previousPosition;
  public IntVec2D previousPosition2D;
  public Vec3 position;
  public IntVec2D position2D;
  public int strength;
  public int dexterity;
  public int constitution;
  public int intelligence;
  public int wisdom;
  public int charisma;
  public int hp;
  public int hpMax;
  public int gold;
  public int energy;
  public int energyMax;
  public int armorClass;
  public int experienceLevel;
  public int experiencePoints;
  public HungerState hungerState;
  public int carryingCapacity;
  public int condition;
  public Alignment alignment;

  public Player() {}

  public String verbose() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("PlayerInfo:%n"));
    sb.append(String.format("Position:%s%n", position));
    sb.append(String.format("Strength:%d%n", strength));
    sb.append(String.format("dexterity:%d%n", dexterity));
    sb.append(String.format("constitution:%d%n", constitution));
    sb.append(String.format("intelligence:%d%n", intelligence));
    sb.append(String.format("wisdom:%d%n", wisdom));
    sb.append(String.format("charisma:%d%n", charisma));
    sb.append(String.format("hp:%d%n", hp));
    sb.append(String.format("hpMax:%d%n", hpMax));
    sb.append(String.format("gold:%d%n", gold));
    sb.append(String.format("energy:%d%n", energy));
    sb.append(String.format("energyMax:%d%n", energyMax));
    sb.append(String.format("armorClass:%d%n", armorClass));
    sb.append(String.format("experienceLevel:%d%n", experienceLevel));
    sb.append(String.format("experiencePoints:%d%n", experiencePoints));
    sb.append(String.format("hungerState:%s%n", hungerState));
    sb.append(String.format("carryingCapacity:%d%n", carryingCapacity));
    sb.append(String.format("condition:%d%n", condition));
    sb.append(String.format("alignment:%s%n", alignment));
    sb.append(System.lineSeparator());
    sb.append(inventory);
    return sb.toString();
  }
}
