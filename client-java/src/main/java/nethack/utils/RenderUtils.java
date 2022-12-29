package nethack.utils;

import nethack.Entity;
import nethack.Color;

public class RenderUtils {
	public static void render(Entity[][] map, Color[][] colors)
	{
		String currentColor = "";
		for (int y = 0; y < 20; y++) {
			String line = "";
			for (int x = 0; x < 79; x++) {
				if (currentColor != colors[y][x].colorCode) {
					line += "\033[" + colors[y][x].colorCode + "m";
				}
				line += map[y][x].symbol;
			}
			System.out.println(line);
		}
		System.out.println("\033[m");
		System.out.println("----------------------");
	}
}