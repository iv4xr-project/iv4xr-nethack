package nethack.object;

import eu.iv4xr.framework.mainConcepts.IPlayer;
import eu.iv4xr.framework.spatial.Vec3;
import java.util.Optional;
import nethack.enums.Alignment;
import nethack.enums.Encumbrance;
import nethack.enums.HungerState;
import util.ColoredStringBuilder;
import util.CustomVec3D;

public class Player implements IPlayer {
  public static final String ID = "player";
  public long lastStutterTimestamp = 0;
  public Inventory inventory;
  public CustomVec3D previousLocation;
  public CustomVec3D location;
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
  public Conditions conditions;
  public Alignment alignment;
  public Optional<Integer> lastPrayerTurn = Optional.empty();

  public Player() {}

  public String verbose() {
    ColoredStringBuilder csb = new ColoredStringBuilder();
    csb.appendf("PlayerInfo:%n");
    csb.appendf("Location:%s%n", location);
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
    csb.appendf("conditions:%s%n", conditions);
    csb.appendf("alignment:%s%n", alignment);
    csb.newLine();
    csb.append(inventory);
    return csb.toString();
  }

  public boolean equals(Object other) {
    if (!(other instanceof Player)) {
      return false;
    }

    Player p = (Player) other;
    return inventory.equals(p.inventory)
        && previousLocation.equals(p.previousLocation)
        && location.equals(p.location)
        && strength == p.strength
        && dexterity == p.dexterity
        && constitution == p.constitution
        && intelligence == p.intelligence
        && wisdom == p.wisdom
        && charisma == p.charisma
        && hp == p.hp
        && hpMax == p.hpMax
        && gold == p.gold
        && energy == p.energy
        && energyMax == p.energyMax
        && armorClass == p.armorClass
        && experienceLevel == p.experienceLevel
        && experiencePoints == p.experiencePoints
        && hungerState.equals(p.hungerState)
        && encumbrance.equals(p.encumbrance)
        && conditions.equals(p.conditions)
        && alignment.equals(p.alignment)
        && lastPrayerTurn.equals(p.lastPrayerTurn);
  }

  @Override
  public Vec3 getPosition() {
    return new Vec3(location.pos.x, location.pos.y, location.lvl);
  }

  @Override
  public void setPosition(Vec3 vec3) {
    location = new CustomVec3D(vec3);
  }

  @Override
  public long getLastStutterTimestamp() {
    return lastStutterTimestamp;
  }

  @Override
  public void setLastStutterTimestamp(long i) {
    this.lastStutterTimestamp = i;
  }
}
