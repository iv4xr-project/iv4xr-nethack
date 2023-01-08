package nethack.object;

import eu.iv4xr.framework.spatial.Vec3;

public class Player {
	public static enum Alignment {
		LAWFUL,
		NEUTRAL,
		CHAOTIC
	}

	public Vec3 position;
	public int strength;
	public int dexterity;
	public int constitution;
	public int intelligence;
	public int wisdom;
	public int charisma;
	public int hp;
	public int hpMax;
	public int gold;
	public int energy;
	public int energyMax;
	public int armorClass;
	public int experienceLevel;
	public int experiencePoints;
	public int hungerState;
	public int carryingCapacity;
	public int condition;
	public Alignment alignment;
	public final String id = "player";

	public Player() {
	}
}