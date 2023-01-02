package connection;

import nethack.object.Stats;
import nethack.object.Player;
import nethack.object.Entity;

public class ObservationMessage {
	public Stats stats;
	public Player player;
	public Entity[][] entities;
	public String message;
	
	public ObservationMessage() {}
}