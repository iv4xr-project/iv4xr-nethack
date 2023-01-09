package nethack.agent;

import nethack.object.EntityType;
import nethack.object.Level;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import eu.iv4xr.framework.extensions.pathfinding.AStar;
import eu.iv4xr.framework.extensions.pathfinding.CanDealWithDynamicObstacle;
import eu.iv4xr.framework.extensions.pathfinding.LayeredAreasNavigation;
import eu.iv4xr.framework.extensions.pathfinding.Navigatable;
import eu.iv4xr.framework.extensions.pathfinding.Pathfinder;
import eu.iv4xr.framework.extensions.pathfinding.Sparse2DTiledSurface_NavGraph;
import eu.iv4xr.framework.extensions.pathfinding.XPathfinder;
import eu.iv4xr.framework.extensions.pathfinding.Sparse2DTiledSurface_NavGraph.Door;
import eu.iv4xr.framework.extensions.pathfinding.Sparse2DTiledSurface_NavGraph.Tile;
import eu.iv4xr.framework.extensions.pathfinding.Sparse2DTiledSurface_NavGraph.Wall;
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
	public LayeredAreasNavigation<Tile, Sparse2DTiledSurface_NavGraph> multiLayerNav;

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
		// creating an instance of navigation graph; setting its
		// configuration etc.
		// The graph is empty when created.
		Sparse2DTiledSurface_NavGraph navg = new Sparse2DTiledSurface_NavGraph();
		multiLayerNav = new LayeredAreasNavigation<>();
		navg.sizeX = Level.WIDTH;
		navg.sizeY = Level.HEIGHT;
		multiLayerNav.addNextArea(navg, null, null, false);
		return this;
	}

	public WorldEntity auxState() {
		return worldmodel().elements.get("aux");
	}

	@Override
	public void updateState(String agentId) {
		super.updateState(agentId);
		// Updating the navigation graph:
		// System.out.println(">>> updateState") ;
		WorldEntity aux = auxState();
		var seenTiles = (Serializable[]) aux.properties.get("visibleTiles");
		for (var entry_ : seenTiles) {
			var entry = (Serializable[]) entry_;
			int levelNumber = (int) entry[0];
			IntVec2D pos = (IntVec2D) entry[1];
			EntityType type = (EntityType) entry[2];
			// System.out.println(">>> registering maze " + mazeId + ", tile " + tile + ": "
			// + type) ;
			if (levelNumber >= multiLayerNav.areas.size()) {
				// detecting a new maze, need to allocate a nav-graph for this maze:
				Sparse2DTiledSurface_NavGraph newNav = new Sparse2DTiledSurface_NavGraph();
				newNav.sizeX = Level.WIDTH;
				newNav.sizeY = Level.HEIGHT;
//				int N = env().app.dungeon.config.worldSize;
//				Tile lowPortal = new Tile(N - 2, 1);
//				Tile highPortal = new Tile(1, 1);
//				multiLayerNav.addNextArea(newNav, lowPortal, highPortal, true);
			}

			multiLayerNav.markAsSeen(new Pair<>(levelNumber, new Tile(pos.x, pos.y)));
			switch (type) {
			case WALL:
				multiLayerNav.addObstacle(new Pair<>(levelNumber, new Wall(pos.x, pos.y)));
				break;
			case CORRIDOR:
			case FLOOR:
				multiLayerNav.removeObstacle(new Pair<>(levelNumber, new Tile(pos.x, pos.y)));
				break;
			case DOOR:
				multiLayerNav.addObstacle(new Pair<>(levelNumber, new Door(pos.x, pos.y)));
				break;
			case MONSTER:
				// not going to represent monsters as non-navigable
				// nav.addNonNavigable(new Door(tile.x,tile.y,true));
				break;
			default:
				// representing potions, scrolls and shrines as doors that we can
				// open or close to enable navigation onto them or not:
				// Made this tile for now
				multiLayerNav.addObstacle(new Pair<>(levelNumber, new Tile(pos.x, pos.y)));
				break;
			}
		}
		// removing entities that are no longer in the game-board, except players:
		var removedEntities = (Serializable[]) aux.properties.get("recentlyRemoved");
		for (var entry_ : removedEntities) {
			var id = (String) entry_;
			if (id.equals("player")) {
				continue;
			}
			this.worldmodel.elements.remove(id);
		}
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
	public List<WorldEntity> adjecentMonsters() {
		var player = worldmodel.elements.get("player");
		Tile p = Utils.toTile((int) this.env().app.gameState.player.position.x,
				(int) this.env().app.gameState.player.position.y);
		List<WorldEntity> ms = worldmodel.elements
				.values().stream().filter(e -> e.type == EntityType.MONSTER.toString()
						&& Utils.levelId(player) == Utils.levelId(e) && Utils.adjacent(p, Utils.toTile(e.position)))
				.collect(Collectors.toList());
		return ms;
	}
}
