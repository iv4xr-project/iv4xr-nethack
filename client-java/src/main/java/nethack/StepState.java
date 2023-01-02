package nethack;

import nethack.object.Stats;
import nethack.object.Level;
import nethack.object.Player;

public class StepState {
	public Stats stats;
	public Player player;
	public Level level;
	public String message;
	public boolean done;
	public Object info;
}
