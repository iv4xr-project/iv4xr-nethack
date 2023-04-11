package connection.messagedecoder;

import java.io.DataInputStream;
import java.io.IOException;
import nethack.enums.Alignment;
import nethack.enums.Encumbrance;
import nethack.enums.HungerState;
import nethack.object.*;
import nl.uu.cs.aplib.utils.Pair;
import util.CustomVec2D;
import util.CustomVec3D;

// Source: https://studytrails.com/2016/09/12/java-google-json-type-adapter/
public class StatsDecoder extends Decoder {
  public static Pair<Stats, Player> decode(DataInputStream input) {
    byte[] byteInformation;
    try {
      byteInformation = input.readNBytes(27 * 4);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    int[] blStats = new int[27];
    for (int i = 0, offset = 0; i < blStats.length; i++, offset += 4) {
      blStats[i] =
          parseInt(
              byteInformation[offset],
              byteInformation[offset + 1],
              byteInformation[offset + 2],
              byteInformation[offset + 3]);
    }

    return inPair(blStats);
  }

  // Source: server-python\lib\nle\win\rl\winrl.cc
  private static Pair<Stats, Player> inPair(int[] values) {
    Player player = new Player();
    Stats stats = new Stats();

    stats.dlvl = new Dlvl(values[12], values[23]);
    player.location = new CustomVec3D(0, new CustomVec2D(values[0], values[1]));
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
    stats.turn = new Turn(values[20]);
    player.hungerState = HungerState.fromValue(values[21]);
    player.encumbrance = Encumbrance.fromValue(values[22]);
    stats.levelNumber = values[24];
    player.conditions = Conditions.fromValue(values[25]);
    player.alignment = Alignment.fromValue(values[26]);

    return new Pair<Stats, Player>(stats, player);
  }
}
