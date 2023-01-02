package nethack.object;

public class Level {
	public Entity[][] map;
	private int nr;
	
	public static final int HEIGHT = 21;
	public static final int WIDTH = 79;
	
	public String id() {
		return "level" + nr;
	}
	
	public Level(int levelNr, Entity[][] entities) {
		this.nr = levelNr;
		this.map = entities;
	}
	
	public void invisibleTiles() {
		System.out.print("Invisible tiles:");
		for (int x = 0; x < WIDTH; x++) {
			for (int y = 0; y < HEIGHT; y++) {
				if (map[y][x].color == Color.TRANSPARENT) {
					System.out.print("(" + x + "," + y + ")");
				}
			}
		}
		System.out.println();
	}
}
