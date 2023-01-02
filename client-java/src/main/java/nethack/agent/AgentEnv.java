package nethack.agent;

import eu.iv4xr.framework.mainConcepts.Iv4xrEnvironment;
import eu.iv4xr.framework.mainConcepts.*;
import eu.iv4xr.framework.spatial.IntVec2D;
import eu.iv4xr.framework.spatial.Vec3;
import nethack.object.Action;
import nethack.object.Entity;
import nethack.object.EntityType;
import nethack.NetHack;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides an implementation of {@link nl.uu.cs.aplib.mainConcepts.Environment}
 * to connect iv4xr/aplib agents to the game MiniDungeon.
 *
 * @author wish
 */
public class AgentEnv extends Iv4xrEnvironment{
	public NetHack app;

	public AgentEnv(NetHack app) {
		this.app = app;
	}

	/**
	 * Observing does not advance the game turn.
	 */
	@Override
	public WorldModel observe(String agentId) {
		WorldModel wom = new WorldModel();
		
		wom.agentId = agentId;
		wom.position = app.gameState.player.position;
		wom.timestamp = app.gameState.stats.time;

		WorldEntity aux = mkGameAuxState();
		wom.elements.put(aux.id, aux);
		for (Player P : thegame().players) {
			wom.elements.put(P.id, toWorldEntity(P));
		}

		// adding visible objects:
		var visibleTiles = thegame().visibleTiles();
		for(var sq : visibleTiles) {
			int mazeId = sq.fst;
			var world = app.dungeon.mazes.get(mazeId).world ;
 			Entity e = world[sq.snd.x][sq.snd.y] ;
			if (e != null) {
				wom.elements.put(e.id, toWorldEntity(e)) ;
			}
		}
		// time-stamp the elements:
		for(var e : wom.elements.values()) {
			e.timestamp = wom.timestamp ;
		}
		return wom ;
	}

	public WorldModel action(Action action) throws IOException {
		app.step(action);
		return observe("player");
	}


	WorldEntity toWorldEntity(Entity e, int x, int y) {
		if (e.type == EntityType.VOID || e.type == EntityType.FLOOR) {
			return null;
		}
		
		WorldEntity we;
		String typeString = e.type.toString();
		int level = app.gameState.stats.levelNumber;
		switch(e.type) {
			case WALL:
			case DOOR:
				we = new WorldEntity("id", typeString, false);
				we.properties.put("level", level);
				break;
			case PLAYER:
				we = new WorldEntity("id", typeString, true);
				we.properties.put("level", level);
				we.properties.put("hp", app.gameState.player.hp);
				we.properties.put("hpmax", app.gameState.player.hpMax);
				break;
			case MONSTER:
				we = new WorldEntity("id", typeString, true);
				we.properties.put("level", level);
				break;
			default:
				return null;
		}
		
		Vec3 position = new Vec3(x, y, 0);
		we.position = position;
		return we;
	}

	WorldEntity mkGameAuxState() {
		WorldEntity aux = new WorldEntity("aux","aux",true);
		aux.properties.put("time", app.gameState.stats.time);
		aux.properties.put("status", app.gameState.done);

		// recently removed objects:
		String[] removed = new String[thegame().recentlyRemoved.size()];
		int k = 0 ;
		for(var id : thegame().recentlyRemoved) {
			removed[k] = id;
			k++;
		}
		aux.properties.put("recentlyRemoved",removed);

		// currently visible tiles:
		var visibleTiles_ = thegame().visibleTiles();
		Serializable[] visibleTiles = new Serializable[visibleTiles_.size()];
		k = 0;
		//var world = app.dungeon.currentMaze(app.dungeon.frodo()).world ;
		for(var tile : visibleTiles_) {
			String etype = "";
			int mazeId = tile.fst;
			var world =  app.dungeon.mazes.get(mazeId).world;
			Entity e = world[tile.snd.x][tile.snd.y];
			if (e != null) {
				etype = e.type.toString();
			}
			Serializable[] entry = { mazeId, tile.snd , etype };
			visibleTiles[k] = entry;
			k++;
		}
		aux.properties.put("visibleTiles",visibleTiles);

		return aux;
	}
}
