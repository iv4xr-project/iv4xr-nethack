package nethack;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import eu.iv4xr.framework.spatial.IntVec2D;

public class Entity {
	public enum EntityType {
		WALL, MONSTER, PLAYER
	}

	public String id;
	/**
	 * Specifying in which maze the entity is located.
	 */
	public int mazeId;

	public int x;
	public int y;
	public EntityType type;

	public IntVec2D pos() {
		return new IntVec2D(x,y) ;
	}

	public static class Wall extends Entity {
		public Wall(int x, int y, String id) {
			this.x = x ; this.y = y ;
			this.id = id ;
			this.type = EntityType.WALL ;
		}
	}

	public static class CombativeEntity extends Entity{
		public int hp ;
		public int hpMax ;
		public int attackRating ;
	}

	public static class Player extends CombativeEntity{
		public PlayerStats Stats;

		public Player(List<Integer> blstats, String id) {
			Stats = new PlayerStats(blstats);
			Update(blstats);

			this.id = id;
			this.type = EntityType.PLAYER;
		}

		public boolean dead() {
			return hp <= 0 ;
		}

		public void Update(List<Integer> blstats) {
			this.x = blstats.get(0);
			this.y = blstats.get(1);
			Stats.Update(blstats);
		}
	}

	public static class PlayerStats {
//		From nleobs.c
		public int Strength;
		public int Dexterity;
		public int Constitution;
		public int Intelligence;
		public int Wisdom;
		public int Charisma;
//		public int Score;
		public int Depth;
		public int Gold;
		public int Energy;
		public int MaxEnergy;
		public int ArmorClass;
		public int MonsterLevel;
		public int ExperienceLevel;
		public int ExperiencePoints;
//		public int Time;
		public int HungerState;
		public int CarryingCapacity;
//		public int DungeonNumber;
//		public int LevelNumber;
		public int Condition;
		public int Alignment;

//		Score = blstats.get(9);
//		Time = blstats.get(20);
//		DungeonNumber = blstats.get(23);
//		LevelNumber = blstats.get(24);

		public PlayerStats(List<Integer> blstats) {
			Update(blstats);
		}

		public void Update(List<Integer> blstats) {
			Strength = blstats.get(2);
			Dexterity = blstats.get(4);
			Constitution = blstats.get(5);
			Intelligence = blstats.get(6);
			Wisdom = blstats.get(7);
			Charisma = blstats.get(8);
			Depth = blstats.get(12);
			Gold = blstats.get(13);
			Energy = blstats.get(14);
			MaxEnergy = blstats.get(15);
			ArmorClass = blstats.get(16);
			ExperienceLevel = blstats.get(18);
			ExperiencePoints = blstats.get(19);
			HungerState = blstats.get(21);
			CarryingCapacity = blstats.get(22);
			Condition = blstats.get(25);
			Alignment = blstats.get(26);
		}
	}

	public static class Monster extends CombativeEntity{
		public Monster(int x, int y, String id) {
			this.x = x ; this.y = y ;
			// BUG found by unit testing
			//hpMax = 20 ;
			//hp = 3 ;
			hp = 3 ;
			hpMax = hp ;
			attackRating = 1 ;
			this.id = id ;
			this.type = EntityType.MONSTER ;
		}
	}
}
