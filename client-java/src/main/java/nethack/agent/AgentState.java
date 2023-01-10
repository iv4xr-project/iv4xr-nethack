package nethack.agent;

import nethack.object.EntityType;
import nethack.object.Level;
import nethack.utils.NethackSurface_NavGraph;
import nethack.utils.NethackSurface_NavGraph.Door;
import nethack.utils.NethackSurface_NavGraph.Tile;
import nethack.utils.NethackSurface_NavGraph.Wall;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import eu.iv4xr.framework.extensions.pathfinding.LayeredAreasNavigation;
import eu.iv4xr.framework.extensions.pathfinding.Navigatable;
import eu.iv4xr.framework.mainConcepts.Iv4xrAgentState;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.IntVec2D;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.Environment;
import nl.uu.cs.aplib.utils.Pair;

/**
 * Provides an implementation of agent state
 * {@link nl.uu.cs.aplib.mainConcepts.SimpleState}. The state defined here
 * inherits from {@link Iv4xrAgentState}, so it also keeps a historical
 * WorldModel.
 *
 * @author wish
 *
 */
public class AgentState extends Iv4xrAgentState<Void> {
	public LayeredAreasNavigation<Tile, NethackSurface_NavGraph> multiLayerNav;

	@Override
	public AgentEnv env() {
		return (AgentEnv) super.env();
	}

	/**
	 * We are not going to keep an Nav-graph, but will instead keep a
	 * layered-nav-graphs.
	 */
	@Override
	public Navigatable<Void> worldNavigation() {
		throw new UnsupportedOperationException();
	}

	/**
	 * We are not going to keep an Nav-graph, but will instead keep a
	 * layered-nav-graphs.
	 */
	@Override
	public AgentState setWorldNavigation(Navigatable<Void> nav) {
		throw new UnsupportedOperationException();
	}

	@Override
	public AgentState setEnvironment(Environment env) {
		super.setEnvironment(env);
		multiLayerNav = new LayeredAreasNavigation<>();
		addNewNavGraph(false);
		return this;
	}

	public WorldEntity auxState() {
		return worldmodel().elements.get("aux");
	}

	private void addNewNavGraph(boolean withPortal) {
		NethackSurface_NavGraph newNav = new NethackSurface_NavGraph();
		newNav.sizeX = Level.WIDTH;
		newNav.sizeY = Level.HEIGHT;
		newNav.diagonalMovementPossible = true;
		
		if (withPortal) {
			Vec3 playerPosition = env().app.gameState.player.position;
			Tile lowPortal = new Tile(playerPosition.x, playerPosition.y);
			Tile highPortal = new Tile(playerPosition.x, playerPosition.y);
			multiLayerNav.addNextArea(newNav, lowPortal, highPortal, true);			
		} else {
			multiLayerNav.addNextArea(newNav, null, null, false);
		}
	}

	@Override
	public void updateState(String agentId) {
		super.updateState(agentId);
		WorldEntity aux = auxState();
		var seenTiles = (Serializable[]) aux.properties.get("visibleTiles");
		for (var entry_ : seenTiles) {
			var entry = (Serializable[]) entry_;
			int levelNumber = (int) entry[0];
			IntVec2D pos = (IntVec2D) entry[1];
			EntityType type = (EntityType) entry[2];

			// If detecting a new maze, need to allocate a nav-graph for this maze:
			if (levelNumber >= multiLayerNav.areas.size()) {
				System.out.println("Adding a new level: " + levelNumber);
				addNewNavGraph(true);
			}

			multiLayerNav.markAsSeen(new Pair<>(levelNumber, new Tile(pos.x, pos.y)));
			switch (type) {
			case VOID:
			case WALL:
			case BOULDER:
				multiLayerNav.addObstacle(new Pair<>(levelNumber, new Wall(pos.x, pos.y)));
				break;
			case CORRIDOR:
			case FLOOR:
				multiLayerNav.removeObstacle(new Pair<>(levelNumber, new Tile(pos.x, pos.y)));
				break;
			case DOOR:
				boolean isOpen = env().app.gameState.level().getEntity(pos).symbol != '+';
				multiLayerNav.addObstacle(new Pair<>(levelNumber, new Door(pos.x, pos.y, isOpen)));
				break;
			case PLAYER:
			case PET:
			case MONSTER:
			default:
				// The players position does not change anything about the navigation
				break;
//			default:
//				// representing potions, scrolls and shrines as doors that we can
//				// open or close to enable navigation onto them or not:
//				// Made this tile for now
////				multiLayerNav.addObstacle(new Pair<>(levelNumber, new Door(pos.x, pos.y)));
//				multiLayerNav.removeObstacle(new Pair<>(levelNumber, new Tile(pos.x, pos.y)));
//				break;
			}
		}
		// removing entities that are no longer in the game-board, except players:
//		var removedEntities = (Serializable[]) aux.properties.get("recentlyRemoved");
//		for (var entry_ : removedEntities) {
//			var id = (String) entry_;
//			if (id.equals("player")) {
//				continue;
//			}
//			this.worldmodel.elements.remove(id);
//		}
	}

	/**
	 * Return the game status (as registered in this state).
	 */
	public boolean gameStatus() {
		var aux = worldmodel.elements.get("aux");
		boolean status = (boolean) aux.properties.get("status");
		int time = (int) aux.properties.get("time");

		return status;
	}

	/**
	 * Check if the agent that owns this state is alive in the game (its hp>0).
	 */
	public boolean agentIsAlive() {
		var a = worldmodel.elements.get(worldmodel.agentId);
		if (a == null) {
			throw new IllegalArgumentException();
		}
		var hp = (Integer) a.properties.get("hp");
		if (hp == null) {
			throw new IllegalArgumentException();
		}
		return hp > 0;
	}

	/**
	 * Return a list of monsters which are currently adjacent to the agent that owns
	 * this state. =
	 */
	public List<WorldEntity> adjacentMonsters() {
		var player = worldmodel.elements.get("player");
		Tile p = Utils.toTile((int) this.env().app.gameState.player.position.x,
				(int) this.env().app.gameState.player.position.y);
		List<WorldEntity> ms = worldmodel.elements
				.values().stream().filter(e -> e.type == EntityType.MONSTER.name()
						&& Utils.levelId(player) == Utils.levelId(e) && Utils.adjacent(p, Utils.toTile(e.position)))
				.collect(Collectors.toList());

		if (ms.size() > 0) {
			System.out.println("FOUND A MONSTER CLOSEBY!!");
			WorldEntity monster = ms.get(0);
			System.out.println(String.format("PLAYER at %s", this.env().app.gameState.player.position.toString()));
			System.out.println(String.format("MONSTER at %s", monster.position.toString()));
		}

		return ms;
	}

	/**
	 * Return a list of doors that are in the current . =
	 */
//	public List<WorldEntity> closedDoors() {
//		var player = worldmodel.elements.get(worldmodel.agentId) ;
//		Tile p = Utils.toTile(player.position) ;
//		List<WorldEntity> ms = worldmodel.elements.values().stream()
//				.filter(e -> e.type.equals(EntityType.DOOR.toString())
//						     && Utils.levelId(player) == Utils.levelId(e))
//				.collect(Collectors.toList()) ;
//		return ms;
//	}
}
