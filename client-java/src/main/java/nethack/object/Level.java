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
}
