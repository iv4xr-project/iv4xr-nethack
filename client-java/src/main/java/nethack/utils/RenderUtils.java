package nethack.utils;

import nethack.object.Level;
import nethack.object.Player;
import nethack.object.Stats;
import nethack.GameState;
import nethack.object.Color;

public class RenderUtils {
	public static void render(GameState gameState)
	{
		System.out.println(gameState.message);
		
		// Render the level with the correct colors
		Color currentColor = null;
		Level level = gameState.level();		
		for (int y = 0; y < level.map.length; y++) {
			String line = "";
			for (int x = 0; x < level.map[0].length; x++) {
				// Color changed so add it to the line
				if (currentColor != level.map[y][x].color) {
					currentColor = level.map[y][x].color;
					line += "\033[" + level.map[y][x].color.colorCode + "m";
				}
				line += gameState.level().map[y][x].symbol;
			}
			System.out.println(line);
		}
		System.out.println("\033[m");
		
		// Print statistics of the player and game
		Player player = gameState.player;
		Stats gameStats = gameState.stats;
		String firstStatsLine = String.format("Pos:(%d, %d) St:%d Dx:%d Co:%d In:%d Wi:%d Ch:%d %s S:%d",
				(int)player.position.x, (int)player.position.y, player.strength, player.dexterity, player.constitution,
				player.intelligence, player.wisdom, player.charisma, player.alignment.name(), gameStats.score);
		String secondStatsLine = String.format("Dlvl:%d $:%d HP:%d(%d) Pw:%d(%d) AC:%d Xp:%d/%d T:%d",
				gameStats.levelNumber, player.gold, player.hp, player.hpMax, player.energy, player.energyMax,
				player.armorClass, player.experienceLevel, player.experiencePoints, gameStats.time);
		System.out.println(firstStatsLine);
		System.out.println(secondStatsLine);
	}
}