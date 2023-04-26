package nethack.object;

import java.io.Serializable;
import nethack.object.info.MonsterInfo;
import util.CustomVec2D;
import util.CustomVec3D;

public class Monster implements Serializable {
  public CustomVec3D loc;
  public CustomVec2D pos;
  public int id;
  public short permId;
  public boolean peaceful;
  public MonsterInfo monsterInfo;

  public Monster(CustomVec2D pos, int id, short permId, boolean peaceful, MonsterInfo monsterInfo) {
    this.loc = new CustomVec3D(0, pos);
    this.pos = pos;
    this.id = id;
    this.permId = permId;
    this.peaceful = peaceful;
    this.monsterInfo = monsterInfo;
  }
}
