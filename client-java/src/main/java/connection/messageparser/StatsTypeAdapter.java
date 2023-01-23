package connection.messageparser;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import connection.ConnectionLoggers;
import eu.iv4xr.framework.spatial.IntVec2D;
import eu.iv4xr.framework.spatial.Vec3;
import java.io.IOException;
import nethack.enums.Alignment;
import nethack.enums.HungerState;
import nethack.object.Player;
import nethack.object.Stats;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Source: https://studytrails.com/2016/09/12/java-google-json-type-adapter/
public class StatsTypeAdapter extends TypeAdapter<Pair<Stats, Player>> {
  static final Logger logger = LogManager.getLogger(ConnectionLoggers.TypeAdapterLogger);

  @Override
  public Pair<Stats, Player> read(JsonReader reader) throws IOException {
    // The first token is the start array
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

  // Source: server-python\lib\nle\win\rl\winrl.cc
  private Pair<Stats, Player> toPair(int[] values) {
    Player player = new Player();
    Stats stats = new Stats();

    player.position = new Vec3(values[0], values[1], 0);
    player.position2D = new IntVec2D(values[0], values[1]);
    player.strength = values[2];
    player.dexterity = values[4];
    player.constitution = values[5];
    player.intelligence = values[6];
    player.wisdom = values[7];
    player.charisma = values[8];
    stats.score = values[9];
    player.hp = values[10];
    player.hpMax = values[11];
    stats.oneIndexDepth = values[12];
    stats.zeroIndexDepth = stats.oneIndexDepth - 1;
    player.gold = values[13];
    player.energy = values[14];
    player.energyMax = values[15];
    player.armorClass = values[16];
    stats.monsterLevel = values[17];
    player.experienceLevel = values[18];
    player.experiencePoints = values[19];
    stats.time = values[20];
    player.hungerState = HungerState.fromValue(values[21]);
    player.carryingCapacity = values[22];
    stats.dungeonNumber = values[23];
    stats.levelNumber = values[24];
    player.condition = values[25];
    player.alignment = Alignment.fromValue(values[26]);

    return new Pair<Stats, Player>(stats, player);
  }

  @Override
  public void write(JsonWriter out, Pair<Stats, Player> stats) throws IOException {}
}
