package connection.messagedecoder;

import connection.ObservationMessage;
import java.io.DataInputStream;
import java.io.IOException;
import nethack.object.*;
import nethack.object.items.Item;
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

      int bytesPerTile = 4;
      int nrTiles = input.readShort();
      byte[] tiles = input.readNBytes(bytesPerTile * nrTiles);
      int offset = 0;
      for (int i = 0; i < nrTiles; i++) {
        int x = tiles[offset];
        int y = tiles[offset + 1];
        observationMessage.tiles[y][x] =
            TileDecoder.decode(x, y, tiles[offset + 2], tiles[offset + 3]);
        offset += bytesPerTile;
      }
      Loggers.ProfilerLogger.trace("READ TILES TOOK: %fs", stopwatch.split());

      offset = 0;
      int bytesPerEntity = 8;
      int nrEntities = input.readShort();
      byte[] entities = input.readNBytes(bytesPerEntity * nrEntities);
      for (int i = 0; i < nrEntities; i++) {
        int x = entities[offset];
        int y = entities[offset + 1];
        char symbol = (char) entities[offset + 2];
        int colorCode = entities[offset + 3];
        int glyph = (entities[offset + 4] << 8) + entities[offset + 5];
        int id = (entities[offset + 6] << 8) + entities[offset + 7];
        observationMessage.entities[y][x] = EntityDecoder.decode(symbol, colorCode, glyph, id);
        offset += bytesPerEntity;
      }
      Loggers.ProfilerLogger.trace("READ ENTITIES TOOK: %f", stopwatch.split());

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
