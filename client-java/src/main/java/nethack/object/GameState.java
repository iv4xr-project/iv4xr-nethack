package nethack.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nethack.NetHackLoggers;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.logging.log4j.Logger;

public class GameState {
  public static final Logger logger = NetHackLoggers.NetHackLogger;

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
    StringBuilder sb = new StringBuilder();
    sb.append(message).append(System.lineSeparator());
    sb.append(getLevel()).append(System.lineSeparator());
    sb.append(stats.verbose()).append(System.lineSeparator());
    sb.append(player.verbose()).append(System.lineSeparator());
    return sb.toString();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(message).append(System.lineSeparator());
    sb.append(getLevel()).append(System.lineSeparator());

    String firstStatsLine =
        String.format(
            "Pos:(%d, %d) St:%d Dx:%d Co:%d In:%d Wi:%d Ch:%d %s S:%d",
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
    String secondStatsLine =
        String.format(
            "Dlvl:%d $:%d HP:%d(%d) Pw:%d(%d) AC:%d Xp:%d/%d T:%d %s",
            stats.depth,
            player.gold,
            player.hp,
            player.hpMax,
            player.energy,
            player.energyMax,
            player.armorClass,
            player.experienceLevel,
            player.experiencePoints,
            stats.time,
            player.hungerState);
    sb.append(firstStatsLine).append(System.lineSeparator());
    sb.append(secondStatsLine).append(System.lineSeparator());

    return sb.toString();
  }
}
