package nethack.object;

import nethack.util.ColoredStringBuilder;

public class Stats {
  public int score;
  public int depth;
  public int monsterLevel;
  public int time;
  public int dungeonNumber;
  public int levelNumber;

  public Stats() {}

  public String verbose() {
    ColoredStringBuilder csb = new ColoredStringBuilder();
    csb.appendf("Stats:%n");
    csb.appendf("score:%s%n", score);
    csb.appendf("depth (1 index):%d%n", depth);
    csb.appendf("monsterLevel:%d%n", monsterLevel);
    csb.appendf("time:%d%n", time);
    csb.appendf("dungeonNumber:%d%n", dungeonNumber);
    csb.appendf("levelNumber:%d%n", levelNumber);
    return csb.toString();
  }
}
