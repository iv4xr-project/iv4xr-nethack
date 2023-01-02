package nethack;

import java.util.ArrayList;
import java.util.List;

import nethack.object.Stats;
import nethack.object.Level;
import nethack.object.Player;

public class GameState {
	public Stats stats;
	public Player player;
	public String message;
	public boolean done;
	public Object info;
	public List<Level> world = new ArrayList<Level>();

	public Level level() {
		return world.get(stats.levelNumber - 1);
	}
}
