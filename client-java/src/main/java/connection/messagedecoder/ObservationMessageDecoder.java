package connection.messagedecoder;

import connection.ObservationMessage;
import java.io.DataInputStream;
import java.io.IOException;
import nethack.object.*;
import nl.uu.cs.aplib.utils.Pair;
import util.Loggers;
import util.Stopwatch;

// Source: https://studytrails.com/2016/09/12/java-google-json-type-adapter/
public class ObservationMessageDecoder extends Decoder {
  public static ObservationMessage decode(DataInputStream input) {
    try {
      ObservationMessage observationMessage = new ObservationMessage();
      Stopwatch stopwatch = new Stopwatch(true);

      int inputByte = input.readByte();
      assert inputByte == DecoderBit.ObservationBit.value;
      Loggers.ProfilerLogger.trace("READ BIT TOOK: %fs", stopwatch.split());

      Pair<Stats, Player> pair = StatsDecoder.decode(input);
      observationMessage.stats = pair.fst;
      observationMessage.player = pair.snd;
      Loggers.ProfilerLogger.trace("READ STATS TOOK: %fs", stopwatch.split());

      observationMessage.message = readString(input);
      Loggers.ProfilerLogger.trace("READ MESSAGE TOOK: %fs", stopwatch.split());

      observationMessage.tiles = TileDecoder.decode(input);
      Loggers.ProfilerLogger.trace("READ TILES TOOK: %fs", stopwatch.split());

      observationMessage.symbols = SymbolDecoder.decode(input);
      Loggers.ProfilerLogger.trace("READ ENTITIES TOOK: %fs", stopwatch.split());

      observationMessage.monsters = MonsterDecoder.decode(input);
      Loggers.ProfilerLogger.trace("READ MONSTERS TOOK: %fs", stopwatch.split());

      observationMessage.entities = EntityDecoder.decode(input);
      Loggers.ProfilerLogger.trace("READ ENTITIES TOOK: %fs", stopwatch.split());

      observationMessage.items = ItemDecoder.decode(input);
      Loggers.ProfilerLogger.trace("READ ITEMS TOOK: %f", stopwatch.split());

      return observationMessage;
    } catch (IOException e) {
      throw new RuntimeException("Unable to read out observation message", e);
    }
  }
}
