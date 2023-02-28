package nethack.object;

import eu.iv4xr.framework.spatial.IntVec2D;
import eu.iv4xr.framework.spatial.Vec3;
import nethack.enums.Alignment;
import nethack.enums.Condition;
import nethack.enums.Encumbrance;
import nethack.enums.HungerState;
import util.ColoredStringBuilder;

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
  public Encumbrance encumbrance;
  public Condition condition;
  public Alignment alignment;

  public Player() {}

  public String verbose() {
    ColoredStringBuilder csb = new ColoredStringBuilder();
    csb.appendf("PlayerInfo:%n");
    csb.appendf("Position:%s%n", position);
    csb.appendf("Strength:%d%n", strength);
    csb.appendf("dexterity:%d%n", dexterity);
    csb.appendf("constitution:%d%n", constitution);
    csb.appendf("intelligence:%d%n", intelligence);
    csb.appendf("wisdom:%d%n", wisdom);
    csb.appendf("charisma:%d%n", charisma);
    csb.appendf("hp:%d%n", hp);
    csb.appendf("hpMax:%d%n", hpMax);
    csb.appendf("gold:%d%n", gold);
    csb.appendf("energy:%d%n", energy);
    csb.appendf("energyMax:%d%n", energyMax);
    csb.appendf("armorClass:%d%n", armorClass);
    csb.appendf("experienceLevel:%d%n", experienceLevel);
    csb.appendf("experiencePoints:%d%n", experiencePoints);
    csb.appendf("hungerState:%s%n", hungerState);
    csb.appendf("encumbrance:%s%n", encumbrance);
    csb.appendf("condition:%s%n", condition);
    csb.appendf("alignment:%s%n", alignment);
    csb.newLine();
    csb.append(inventory);
    return csb.toString();
  }
}
