package nethack.object;

import nethack.object.info.EntityInfo;
import util.CustomVec2D;
import util.CustomVec3D;

public class Entity {
  public CustomVec3D loc;
  public CustomVec2D pos;
  public int id;
  public EntityInfo entityInfo;
  public Turn createTurn;
  public int quantity;

  public Entity(CustomVec2D pos, int id, EntityInfo entityInfo, Turn createTurn, int quantity) {
    this.loc = new CustomVec3D(0, pos);
    this.pos = pos;
    this.id = id;
    this.entityInfo = entityInfo;
    this.createTurn = createTurn;
    this.quantity = quantity;
  }

  public String toId() {
    return String.format("%s_%d", entityInfo.entityClass, id);
  }

  public String toString() {
    return String.format(
        "%s (%d) %s (%d %s) T=%s",
        pos, id, entityInfo.entityClass, quantity, entityInfo, createTurn);
  }
}
