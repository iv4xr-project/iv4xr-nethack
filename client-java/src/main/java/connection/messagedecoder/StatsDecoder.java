package connection.messagedecoder;

import connection.ObservationMessage;
import eu.iv4xr.framework.spatial.IntVec2D;
import eu.iv4xr.framework.spatial.Vec3;
import java.io.DataInputStream;
import java.io.IOException;
import nethack.enums.Alignment;
import nethack.enums.HungerState;

// Source: https://studytrails.com/2016/09/12/java-google-json-type-adapter/
public class StatsDecoder extends Decoder {
  public static void decode(DataInputStream input, ObservationMessage observationMessage) {
    try {
      byte[] byteInformation = input.readNBytes(27 * 4);
      int[] blStats = new int[27];
      for (int i = 0; i < blStats.length; i++) {
        blStats[i] =
            ((byteInformation[i * 4] << 24)
                + (byteInformation[i * 4 + 1] << 16)
                + (byteInformation[i * 4 + 2] << 8)
                + (byteInformation[i * 4 + 3]));
      }
      populate(blStats, observationMessage);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // Source: server-python\lib\nle\win\rl\winrl.cc
  private static void populate(int[] values, ObservationMessage observationMessage) {
    observationMessage.stats.oneIndexDepth = values[12];
    observationMessage.stats.zeroIndexDepth = values[12] - 1;

    observationMessage.player.position = new Vec3(values[0], values[1], values[12] - 1);
    observationMessage.player.position2D = new IntVec2D(values[0], values[1]);
    observationMessage.player.strength = values[2];
    observationMessage.player.dexterity = values[4];
    observationMessage.player.constitution = values[5];
    observationMessage.player.intelligence = values[6];
    observationMessage.player.wisdom = values[7];
    observationMessage.player.charisma = values[8];
    observationMessage.stats.score = values[9];
    observationMessage.player.hp = values[10];
    observationMessage.player.hpMax = values[11];
    observationMessage.player.gold = values[13];
    observationMessage.player.energy = values[14];
    observationMessage.player.energyMax = values[15];
    observationMessage.player.armorClass = values[16];
    observationMessage.stats.monsterLevel = values[17];
    observationMessage.player.experienceLevel = values[18];
    observationMessage.player.experiencePoints = values[19];
    observationMessage.stats.time = values[20];
    observationMessage.player.hungerState = HungerState.fromValue(values[21]);
    observationMessage.player.carryingCapacity = values[22];
    observationMessage.stats.dungeonNumber = values[23];
    observationMessage.stats.levelNumber = values[24];
    observationMessage.player.condition = values[25];
    observationMessage.player.alignment = Alignment.fromValue(values[26]);
  }
}
