package nethack.object.info;

import nethack.enums.ArmorType;
import nethack.enums.EntityClass;
import nethack.enums.Skill;

public class EntityInfo {
  public int index;
  public String name;
  public String description;
  public int weight;
  public int cost;
  public Skill skill;
  public EntityClass entityClass;
  public boolean missile;
  public boolean fromLauncher;
  public ArmorType armorType;

  public EntityInfo() {}

  public String toString() {
    return String.format(
        "%s:%s(%s) %s %s weight=%d cost=%d",
        index, name, description, entityClass, skill, weight, cost);
  }
}
