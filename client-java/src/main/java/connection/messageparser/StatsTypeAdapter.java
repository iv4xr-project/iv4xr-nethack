package connection.messageparser;

import nethack.object.Player;
import nethack.object.Player.Alignment;
import nl.uu.cs.aplib.utils.Pair;
import eu.iv4xr.framework.spatial.Vec3;
import nethack.object.Stats;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

// Source: https://studytrails.com/2016/09/12/java-google-json-type-adapter/
public class StatsTypeAdapter extends TypeAdapter<Pair<Stats, Player>> {	
	@Override
	public Pair<Stats, Player> read(JsonReader reader) throws IOException {
		// the first token is the start array
		int[] values = new int[27];
		JsonToken token = reader.peek();
		if (token.equals(JsonToken.BEGIN_ARRAY)) {
			reader.beginArray();
			int i = 0;
			while (!reader.peek().equals(JsonToken.END_ARRAY)) {
				values[i++] = reader.nextInt();
			}
			reader.endArray();
		}
		
		return toPair(values);
	}
	
	private Pair<Stats, Player> toPair(int[] values) {
		Player player = new Player();
		Stats stats = new Stats();
		
		player.position = new Vec3(values[0], values[1], 0);
		player.strength = values[2];
		player.dexterity = values[4];
		player.constitution = values[5];
		player.intelligence = values[6];
		player.wisdom = values[7];
		player.charisma = values[8];
		stats.score = values[9];
		player.hp = values[10];
		player.hpMax = values[11];
		stats.depth = values[12];
		player.gold = values[13];
		player.energy = values[14];
		player.energyMax = values[15];
		player.armorClass = values[16];
		player.experienceLevel = values[18];
		player.experiencePoints = values[19];
		stats.time = values[20];
		player.hungerState = values[21];
		player.carryingCapacity = values[22];
//		gameState.dungeonNumber = values[23];
		stats.levelNumber = values[24];
		player.condition = values[25];
				
		switch(values[26]) { 
			case -1:
				player.alignment = Alignment.CHAOTIC;
				break;
			case 0:
				player.alignment = Alignment.NEUTRAL;
				break;
			case 1:
				player.alignment = Alignment.LAWFUL;
				break;
			default:
				System.out.println("Alignment value " + values[26] + " not valid");
				return null;
		}
		
		return new Pair<Stats, Player>(stats, player);
	}
	
	@Override
	public void write(JsonWriter out, Pair<Stats, Player> stats) throws IOException { }
}