package connection.messagedecoder;

import connection.ObservationMessage;
import java.io.DataInputStream;
import java.io.IOException;
import nethack.object.*;
import nethack.object.items.Item;
import nethack.world.Level;
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

      int bytesPerEntry = 8;
      byte[] entities = input.readNBytes(bytesPerEntry * Level.SIZE.width * Level.SIZE.height);
      int offset = 0;
      for (int y = 0; y < Level.SIZE.height; y++) {
        for (int x = 0; x < Level.SIZE.width; x++) {
          char symbol = (char) entities[offset];
          int colorCode = entities[offset + 1];
          int glyph = (entities[offset + 2] << 8) + entities[offset + 3];
          int id = (entities[offset + 7] << 8) + entities[offset + 6];
          observationMessage.entities[y][x] = EntityDecoder.decode(symbol, colorCode, glyph, id);
          observationMessage.tiles[y][x] =
              TileDecoder.decode(x, y, entities[offset + 4], entities[offset + 5]);
          offset += bytesPerEntry;
        }
      }

      Loggers.ProfilerLogger.trace("READ MAP TOOK: %f", stopwatch.split());

      int nr_items = input.readByte();
      observationMessage.items = new Item[nr_items];

      for (int i = 0; i < nr_items; i++) {
        observationMessage.items[i] = ItemDecoder.decode(input);
      }
      Loggers.ProfilerLogger.trace("READ ITEMS TOOK: %f", stopwatch.split());

      return observationMessage;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
