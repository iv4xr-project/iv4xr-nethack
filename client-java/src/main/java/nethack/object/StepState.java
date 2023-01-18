package nethack.object;

import nethack.object.Level;
import nethack.object.Player;
import nethack.object.Stats;

public class StepState {
    public Stats stats;
    public Player player;
    public Level level;
    public String message;
    public boolean done;
    public Object info;
}
