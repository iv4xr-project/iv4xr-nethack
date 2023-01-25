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
      if (input.readByte() != DecoderBit.ObservationBit.bitValue) {
        logger.fatal("Did not receive observation message byte");
        System.exit(-1);
      }

      Pair<Stats, Player> blStats = StatsDecoder.decode(input);
      observationMessage.stats = blStats.fst;
      observationMessage.player = blStats.snd;
      observationMessage.message = readString(input);

      observationMessage.entities = new Entity[Level.HEIGHT][Level.WIDTH];
      for (int y = 0; y < Level.HEIGHT; y++) {
        for (int x = 0; x < Level.WIDTH; x++) {
          observationMessage.entities[y][x] = EntityDecoder.decode(input);
        }
      }

      int nr_items = input.readByte();
      observationMessage.items = new Item[nr_items];

      for (int i = 0; i < nr_items; i++) {
        observationMessage.items[i] = ItemDecoder.decode(input);
      }

      return observationMessage;

    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
