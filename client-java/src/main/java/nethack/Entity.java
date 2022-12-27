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

	public static class Monster extends CombativeEntity{
		public Monster(int x, int y, String id) {
			this.x = x ; this.y = y ;
			this.id = id ;
			this.type = EntityType.MONSTER ;
		}
	}
}
