package nethack.object.info;

import java.io.Serializable;
import nethack.enums.MonsterType;

public class MonsterInfo implements Serializable {
  public int index;
  public String name;
  public MonsterType monsterType;
  public int level;
  public int movementSpeed;
  public int armorClass;
  public int magicResistance;
  public int corpseWeight;
  public int corpseNutrition;
  public int resistances;
  public int difficulty;

  public MonsterInfo() {}
}
