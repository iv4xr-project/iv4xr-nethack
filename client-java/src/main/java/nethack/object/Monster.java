package nethack.object;

import nethack.object.info.MonsterInfo;
import util.CustomVec2D;

public class Monster {
  public CustomVec2D pos;
  public int id;
  public short permId;
  public boolean peaceful;
  public MonsterInfo monsterInfo;

  public Monster(CustomVec2D pos, int id, short permId, boolean peaceful, MonsterInfo monsterInfo) {
    this.pos = pos;
    this.id = id;
    this.permId = permId;
    this.peaceful = peaceful;
    this.monsterInfo = monsterInfo;
  }
}
