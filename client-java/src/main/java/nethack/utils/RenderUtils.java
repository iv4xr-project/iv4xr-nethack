package nethack.utils;

import nethack.Entity;
import nethack.Color;

public class RenderUtils {
	public static void render(Entity[][] map)
	{
		Color currentColor = null;
		
		for (int y = 0; y < map.length; y++) {
			String line = "";
			for (int x = 0; x < map[0].length; x++) {
				// Color changed so add it to the line
				if (currentColor != map[y][x].color) {
					currentColor = map[y][x].color;
					line += "\033[" + map[y][x].color.colorCode + "m";
				}
				line += map[y][x].symbol;
			}
			System.out.println(line);
		}
		System.out.println("\033[m");
	}
}