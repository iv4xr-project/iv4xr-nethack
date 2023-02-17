package nethack.object;

import util.ColoredStringBuilder;

public class Stats {
  public int score;
  public int monsterLevel;
  public Turn turn;
  public Dlvl dlvl;
  public int levelNumber;

  public Stats() {}

  public String verbose() {
    ColoredStringBuilder csb = new ColoredStringBuilder();
    csb.appendf("Stats:%n");
    csb.appendf("score:%s%n", score);
    csb.appendf("depth:%d%n", dlvl.depth);
    csb.appendf("monsterLevel:%d%n", monsterLevel);
    csb.appendf("turn:%d(%d)%n", turn.time, turn.turnNr);
    csb.appendf("dungeonNumber:%d%n", dlvl.dungeonNumber);
    csb.appendf("levelNumber:%d%n", levelNumber);
    return csb.toString();
  }
}
