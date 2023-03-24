package nethack.object;

import nethack.enums.Color;
import nethack.world.Dungeon;
import nethack.world.Level;
import util.ColoredStringBuilder;

public class GameState {
  public Stats stats;
  public Player player;
  public String message;
  public boolean done;
  public Object info;
  public Dungeon dungeon = new Dungeon();

  public Level getLevel() {
    assert stats != null : "Cannot retrieve level from GameState without stats";
    if (!dungeon.levelExists(stats.dlvl)) {
      return null;
    }
    return dungeon.getLevel(stats.dlvl);
  }

  public int getLevelNr() {
    return dungeon.getLevelNr(stats.dlvl);
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
