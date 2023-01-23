package nethack.object;

public class Stats {
  public int score;
  public int zeroIndexDepth;
  public int oneIndexDepth;
  public int monsterLevel;
  public int time;
  public int dungeonNumber;
  public int levelNumber;

  public Stats() {}

  public String verbose() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("Stats:%n"));
    sb.append(String.format("score:%s%n", score));
    sb.append(String.format("depth (1 index):%d%n", oneIndexDepth));
    sb.append(String.format("monsterLevel:%d%n", monsterLevel));
    sb.append(String.format("time:%d%n", time));
    sb.append(String.format("dungeonNumber:%d%n", dungeonNumber));
    sb.append(String.format("levelNumber:%d%n", levelNumber));
    return sb.toString();
  }
}
