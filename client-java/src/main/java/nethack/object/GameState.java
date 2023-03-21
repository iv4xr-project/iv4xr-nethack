package nethack.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nethack.enums.Color;
import util.ColoredStringBuilder;

public class GameState {
  public Stats stats;
  public Player player;
  public String message;
  public boolean done;
  public Object info;
  private final List<Level> world = new ArrayList<>();
  private final Map<Dlvl, Integer> indexes = new HashMap<>();

  public Level getLevel() {
    assert stats != null : "Cannot retrieve level from GameState without stats";
    if (!indexes.containsKey(stats.dlvl)) {
      return null;
    }
    return world.get(getLevelNr());
  }

  // Assumes the stats are already updated
  public void setLevel(Level level) {
    Level previousLevel = getLevel();
    if (!indexes.containsKey(stats.dlvl)) {
      indexes.put(stats.dlvl, world.size());
      world.add(null);
    }
    level.setChangedCoordinates(previousLevel);
    world.set(getLevelNr(), level);
  }

  public int getLevelNr() {
    return indexes.get(stats.dlvl);
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
        "@%s St:%d Dx:%d Co:%d In:%d Wi:%d Ch:%d %s S:%d%n",
        player.location,
        player.strength,
        player.dexterity,
        player.constitution,
        player.intelligence,
        player.wisdom,
        player.charisma,
        player.alignment.name(),
        stats.score);
    csb.appendf(
        "Dlvl:%d(%d) %s$:%s%d %sHP:%s%d(%d) %sPw:%s%d(%d) %sAC:%s%d Xp:%d/%d T:%d(%d)"
            + " \uD83C\uDF54:%s \uD83D\uDCAA:%s \uD83D\uDE03:%s%n",
        stats.dlvl.depth,
        stats.dlvl.dungeonNumber,
        Color.YELLOW,
        Color.RESET,
        player.gold,
        Color.RED,
        Color.RESET,
        player.hp,
        player.hpMax,
        Color.BLUE,
        Color.RESET,
        player.energy,
        player.energyMax,
        Color.WHITE,
        Color.RESET,
        player.armorClass,
        player.experienceLevel,
        player.experiencePoints,
        stats.turn.time,
        stats.turn.step,
        player.hungerState,
        player.encumbrance,
        player.conditions);

    return csb.toString();
  }
}
