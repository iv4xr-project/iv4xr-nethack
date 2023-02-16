package nethack.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.uu.cs.aplib.utils.Pair;
import util.ColoredStringBuilder;

public class GameState {
  public Stats stats;
  public Player player;
  public String message;
  public boolean done;
  public Object info;
  private final List<Level> world = new ArrayList<>();
  private int currentLevelNr;
  private Pair<Integer, Integer> currentIndex;
  private final Map<Pair<Integer, Integer>, Integer> indexes = new HashMap<>();

  public Level getLevel() {
    assert stats != null : "Cannot retrieve level from GameState without stats";
    Pair<Integer, Integer> index = createIndex();
    if (!index.equals(currentIndex)) {
      if (!indexes.containsKey(index)) {
        return null;
      }
      currentIndex = index;
      currentLevelNr = indexes.get(currentIndex);
    }

    return world.get(currentLevelNr);
  }

  // Assumes the stats are already updated
  public void setLevel(Level level) {
    Level previousLevel = getLevel();
    Pair<Integer, Integer> index = createIndex();
    if (!index.equals(currentIndex)) {
      if (!indexes.containsKey(index)) {
        indexes.put(index, world.size());
        world.add(null);
      }

      currentIndex = index;
      currentLevelNr = indexes.get(currentIndex);
    }

    level.setChangedCoordinates(previousLevel);
    world.set(currentLevelNr, level);
  }

  public int getLevelNr() {
    return indexes.get(createIndex());
  }

  private Pair<Integer, Integer> createIndex() {
    return new Pair<>(stats.depth, stats.dungeonNumber);
  }

  public String verbose() {
    ColoredStringBuilder csb = new ColoredStringBuilder();
    csb.append(message).newLine();
    csb.append(getLevel()).newLine();
    csb.append(stats.verbose()).newLine();
    csb.append(player.verbose()).newLine();
    return csb.toString();
  }

  @Override
  public String toString() {
    ColoredStringBuilder csb = new ColoredStringBuilder();
    csb.append(message).newLine();
    csb.append(getLevel()).newLine();
    csb.appendf(
        "Pos:(%d, %d) St:%d Dx:%d Co:%d In:%d Wi:%d Ch:%d %s S:%d%n",
        player.position2D.x,
        player.position2D.y,
        player.strength,
        player.dexterity,
        player.constitution,
        player.intelligence,
        player.wisdom,
        player.charisma,
        player.alignment.name(),
        stats.score);
    csb.appendf(
        "Dlvl:%d $:%d HP:%d(%d) Pw:%d(%d) AC:%d Xp:%d/%d T:%d(%d) %s%n",
        stats.depth,
        player.gold,
        player.hp,
        player.hpMax,
        player.energy,
        player.energyMax,
        player.armorClass,
        player.experienceLevel,
        player.experiencePoints,
        stats.turn.time,
        stats.turn.step,
        player.hungerState);

    return csb.toString();
  }
}
