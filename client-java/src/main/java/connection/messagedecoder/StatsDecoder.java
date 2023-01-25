package connection.messagedecoder;

import eu.iv4xr.framework.spatial.IntVec2D;
import eu.iv4xr.framework.spatial.Vec3;
import java.io.DataInputStream;
import java.io.IOException;
import nethack.enums.Alignment;
import nethack.enums.HungerState;
import nethack.object.Player;
import nethack.object.Stats;
import nl.uu.cs.aplib.utils.Pair;

// Source: https://studytrails.com/2016/09/12/java-google-json-type-adapter/
public class StatsDecoder extends Decoder {
  public static Pair<Stats, Player> decode(DataInputStream input) {
    try {
      int[] blStats = new int[27];
      for (int i = 0; i < blStats.length; i++) {
        blStats[i] = input.readInt();
      }
      return toPair(blStats);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // Source: server-python\lib\nle\win\rl\winrl.cc
  private static Pair<Stats, Player> toPair(int[] values) {
    Player player = new Player();
    Stats stats = new Stats();

    stats.oneIndexDepth = values[12];
    stats.zeroIndexDepth = stats.oneIndexDepth - 1;

    player.position = new Vec3(values[0], values[1], stats.zeroIndexDepth);
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
}
