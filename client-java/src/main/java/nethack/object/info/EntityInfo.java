package nethack.object.info;

public class EntityInfo {
  public int index;
  public String name;
  public String description;
  public int weight;
  public int cost;

  public EntityInfo() {}

  public String toString() {
    return String.format(
        "EntityInfo (index=%d) %s (%s) weight=%d cost=%d", index, name, description, weight, cost);
  }
}
