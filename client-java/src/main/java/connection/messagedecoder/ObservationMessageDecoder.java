package connection.messagedecoder;

import connection.ObservationMessage;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import nethack.object.*;

// Source: https://studytrails.com/2016/09/12/java-google-json-type-adapter/
public class ObservationMessageDecoder extends Decoder {
  public static ObservationMessage decode(DataInputStream input) {
    try {
      ObservationMessage observationMessage = new ObservationMessage();
      boolean verbose = false;

      long now = System.nanoTime();
      if (input.readByte() != DecoderBit.ObservationBit.value) {
        logger.fatal("Did not receive observation message byte");
        System.exit(-1);
      }
      long now_bit = System.nanoTime();
      if (verbose) System.out.printf("READ BIT TOOK: %d%n", now_bit - now);

      StatsDecoder.decode(input, observationMessage);

      long now_blstats = System.nanoTime();
      if (verbose) System.out.printf("READ BLSTATS TOOK: %d%n", now_blstats - now_bit);

      byte[] chars = input.readNBytes(256);
      observationMessage.message = new String(chars, StandardCharsets.UTF_8);

      long now_1 = System.nanoTime();
      if (verbose) System.out.printf("READ MESSAGE TOOK: %d%n", now_1 - now_blstats);

      long total = 0;
      for (int y = 0; y < Level.HEIGHT; y++) {
        byte[] entities = input.readNBytes(4 * Level.WIDTH);
        for (int x = 0; x < Level.WIDTH; x++) {
          long inbetween = System.nanoTime();
          char symbol = (char) entities[x * 4];
          int colorCode = entities[x * 4 + 1];
          int glyph = (entities[x * 4 + 2] << 8) + entities[x * 4 + 3];
          observationMessage.entities[y][x] = EntityDecoder.decode(input, symbol, colorCode, glyph);
          long time = System.nanoTime();
          total += time - inbetween;
          //          if (time - inbetween > 30000) System.out.printf("Add entity took: %d%n", time
          // - inbetween);
        }
        //        if (verbose) System.out.printf("READ ROW TOOK: %d%n", System.nanoTime() -
        // inbetween);
      }
      long now_2 = System.nanoTime();
      if (verbose) System.out.printf("READ MAP TOOK: %d%n", now_2 - now_1);

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
