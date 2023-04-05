package nethack.object;

import util.CustomVec2D;

public class Monster {
  public CustomVec2D pos;
  public int id;
  public short permId;
  public boolean peaceful;

  public Monster(CustomVec2D pos, int id, short permId, boolean peaceful) {
    this.pos = pos;
    this.id = id;
    this.permId = permId;
    this.peaceful = peaceful;
  }
}
