package nethack;

public class Blstats {
//	Source: server-python/lib/nle/include/nleobs.h
	public int x;
	public int y;
	public int strength;
	public int dexterity;
	public int constitution;
	public int intelligence;
	public int wisdom;
	public int charisma;
	public int score;
	public int hp;
	public int hpMax;
	public int depth;
	public int gold;
	public int energy;
	public int maxEnergy;
	public int armorClass;
	public int monsterLevel;
	public int experienceLevel;
	public int experiencePoints;
	public int time;
	public int hungerState;
	public int carryingCapacity;
	public int dungeonNumber;
	public int levelNumber;
	public int condition;
	public int alignment;

	public Blstats(int[] blstats) {
		Update(blstats);
	}

	public void Update(int[] blstats) {
		x = blstats[0];
		y = blstats[1];
		strength = blstats[2];
		dexterity = blstats[4];
		constitution = blstats[5];
		intelligence = blstats[6];
		wisdom = blstats[7];
		charisma = blstats[8];
		score = blstats[9];
		hp = blstats[10];
		hpMax = blstats[11];
		depth = blstats[12];
		gold = blstats[13];
		energy = blstats[14];
		maxEnergy = blstats[15];
		armorClass = blstats[16];
		experienceLevel = blstats[18];
		experiencePoints = blstats[19];
		time = blstats[20];
		hungerState = blstats[21];
		carryingCapacity = blstats[22];
		dungeonNumber = blstats[23];
		levelNumber = blstats[24];
		condition = blstats[25];
		alignment = blstats[26];
	}
}
