package nethack;

import nethack.object.Level;
import nethack.object.Player;
import nethack.object.Stats;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    public Stats stats;
    public Player player;
    public String message;
    public boolean done;
    public Object info;
    public List<Level> world = new ArrayList<Level>();

    public Level level() {
        if (stats == null) {
            return null;
        }
        if (stats.zeroIndexLevelNumber < 0) {
            return null;
        }
        return world.get(stats.zeroIndexLevelNumber);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(message).append(System.lineSeparator());
        sb.append(level()).append(System.lineSeparator());

        String firstStatsLine = String.format("Pos:(%d, %d) St:%d Dx:%d Co:%d In:%d Wi:%d Ch:%d %s S:%d",
                player.position2D.x, player.position2D.y, player.strength, player.dexterity,
                player.constitution, player.intelligence, player.wisdom, player.charisma, player.alignment.name(),
                stats.score);
        String secondStatsLine = String.format("Dlvl:%d $:%d HP:%d(%d) Pw:%d(%d) AC:%d Xp:%d/%d T:%d",
                stats.oneIndexLevelNumber, player.gold, player.hp, player.hpMax, player.energy, player.energyMax,
                player.armorClass, player.experienceLevel, player.experiencePoints, stats.time);
        sb.append(firstStatsLine).append(System.lineSeparator());
        sb.append(secondStatsLine).append(System.lineSeparator());

        return sb.toString();
    }
}
