package nethack.object;

import nethack.enums.EntityClass;
import nethack.object.info.EntityInfo;
import util.CustomVec2D;

public class Entity {
  public CustomVec2D pos;
  public int id;
  public EntityClass entityClass;
  public EntityInfo entityInfo;
  public Turn createTurn;
  public int quantity;

  public Entity(
      CustomVec2D pos,
      int id,
      EntityClass entityClass,
      EntityInfo entityInfo,
      Turn createTurn,
      int quantity) {
    this.pos = pos;
    this.id = id;
    this.entityClass = entityClass;
    this.entityInfo = entityInfo;
    this.createTurn = createTurn;
    this.quantity = quantity;
  }

  public String toId() {
    return String.format("%s_%d", entityClass, id);
  }

  public String toString() {
    return String.format(
        "%s (%d) %s (%d %s) T=%s", pos, id, entityClass, quantity, entityInfo, createTurn);
  }
}
