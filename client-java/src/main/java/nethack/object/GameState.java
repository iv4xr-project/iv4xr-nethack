package nethack.object;

import java.util.ArrayList;
import java.util.List;
import nethack.NetHackLoggers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameState {
  public static final Logger logger = LogManager.getLogger(NetHackLoggers.NetHackLogger);

  public Stats stats;
  public Player player;
  public String message;
  public boolean done;
  public Object info;
  public List<Level> world = new ArrayList<>();

  public Level level() {
    if (stats == null) {
      logger.warn("Cannot retrieve level from GameState without stats, return null");
      return null;
    }
    if (stats.zeroIndexDepth < 0) {
      logger.warn("Cannot retrieve depth < 0, return null");
      return null;
    }
    return world.get(stats.zeroIndexDepth);
  }

  public String verbose() {
    StringBuilder sb = new StringBuilder();
    sb.append(message).append(System.lineSeparator());
    sb.append(level()).append(System.lineSeparator());
    sb.append(stats.verbose()).append(System.lineSeparator());
    sb.append(player.verbose()).append(System.lineSeparator());
    return sb.toString();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(message).append(System.lineSeparator());
    sb.append(level()).append(System.lineSeparator());

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
            stats.oneIndexDepth,
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
