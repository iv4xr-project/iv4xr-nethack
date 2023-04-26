package nethack.object;

import eu.iv4xr.framework.mainConcepts.IWorldEntity;
import java.io.Serializable;
import nethack.object.info.EntityInfo;
import util.CustomVec2D;
import util.CustomVec3D;

public class Entity implements IWorldEntity, Serializable {
  public CustomVec3D loc;
  public CustomVec2D pos;
  public int id;
  public EntityInfo entityInfo;
  public Turn createTurn;
  public int quantity;
  public long timestamp;
  public long stutterTimestamp;

  public Entity(CustomVec2D pos, int id, EntityInfo entityInfo, Turn createTurn, int quantity) {
    this.loc = new CustomVec3D(0, pos);
    this.pos = pos;
    this.id = id;
    this.entityInfo = entityInfo;
    this.createTurn = createTurn;
    this.quantity = quantity;
  }

  public String toString() {
    return String.format(
        "%s (%d) %s (%d %s) T=%s",
        pos, id, entityInfo.entityClass, quantity, entityInfo, createTurn);
  }

  @Override
  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public void setTimestamp(long i) {
    this.timestamp = i;
  }

  @Override
  public long getLastStutterTimestamp() {
    return stutterTimestamp;
  }

  @Override
  public void setLastStutterTimestamp(long i) {
    this.stutterTimestamp = i;
  }

  @Override
  public String getId() {
    return String.format("%s_%d", entityInfo.entityClass, id);
  }

  public void setId(String id) {
    throw new RuntimeException("ID derived from properties");
  }

  public boolean equals(Object other) {
    if (!(other instanceof Entity)) {
      return false;
    }

    Entity e = (Entity) other;
    return loc.equals(e.loc)
        && id == e.id
        && entityInfo.equals(e.entityInfo)
        && createTurn.equals(e.createTurn)
        && quantity == e.quantity;
  }
}
