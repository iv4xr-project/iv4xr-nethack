package connection.messagedecoder;

import connection.ObservationMessage;
import java.io.DataInputStream;
import java.io.IOException;
import nethack.object.*;
import nl.uu.cs.aplib.utils.Pair;

// Source: https://studytrails.com/2016/09/12/java-google-json-type-adapter/
public class ObservationMessageDecoder extends Decoder {
  public static ObservationMessage decode(DataInputStream input) {
    try {
      ObservationMessage observationMessage = new ObservationMessage();
      boolean verbose = true;

      long now = System.nanoTime();
      if (input.readByte() != DecoderBit.ObservationBit.value) {
        logger.fatal("Did not receive observation message byte");
        System.exit(-1);
      }
      long now_bit = System.nanoTime();
      if (verbose) System.out.printf("READ BIT TOOK: %d%n", now_bit - now);

      Pair<Stats, Player> blStats = StatsDecoder.decode(input);
      observationMessage.stats = blStats.fst;
      observationMessage.player = blStats.snd;
      observationMessage.message = readString(input);
      long now_1 = System.nanoTime();
      if (verbose) System.out.printf("READ BLSTATS TOOK: %d%n", now_1 - now_bit);

      observationMessage.entities = new Entity[Level.HEIGHT][Level.WIDTH];
      //      while (true) {
      ////        byte x = Decoder.readByte(input);
      ////        byte y = Decoder.readByte(input);
      //        Entity e = EntityDecoder.decode(input);
      //        observationMessage.entities[y][x] = EntityDecoder.decode(input);
      //        if (x == 0 && y == 0 && e.glyph == 0) {
      //          break;
      //        }
      //      }

      for (int y = 0; y < Level.HEIGHT; y++) {
        for (int x = 0; x < Level.WIDTH; x++) {
          observationMessage.entities[y][x] = EntityDecoder.decode(input);
        }
      }
      long now_2 = System.nanoTime();
      if (verbose) System.out.printf("READ MAP TOOK: %d%n", now_2 - now_1);

      //      for (int y = 0; y < Level.HEIGHT; y++) {
      //        for (int x = 0; x < Level.WIDTH; x++) {
      //          if (observationMessage.entities[y][x] == null) {
      //            observationMessage.entities[y][x] = new Entity(2359, ' ', EntityType.VOID,
      // Color.BLACK);
      //          }
      //        }
      //      }
      //      long map_now = System.nanoTime();
      //      if (verbose) System.out.printf("READ FILL IN MAP TOOK: %d%n", map_now-now_2);

      int nr_items = input.readByte();
      observationMessage.items = new Item[nr_items];

      for (int i = 0; i < nr_items; i++) {
        observationMessage.items[i] = ItemDecoder.decode(input);
      }
      long now_3 = System.nanoTime();
      if (verbose) System.out.printf("READ ITEMS TOOK: %d%n", now_3 - now_2);

      return observationMessage;

    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
