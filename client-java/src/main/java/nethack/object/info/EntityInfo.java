package nethack.object.info;

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

  public EntityInfo() {}

  public String toString() {
    return String.format(
        "%s:%s(%s) weight=%d cost=%d %s", index, name, description, weight, cost, skill);
  }
}
