package connection.messagedecoder;

import java.io.DataInputStream;
import java.io.IOException;
import nethack.object.Seed;

public class SeedDecoder extends Decoder {
  public static Seed decode(DataInputStream input) {
    try {

      if (input.readByte() != DecoderBit.SeedBit.bitValue) {
        logger.fatal("Did not receive seed message byte");
        System.exit(-1);
      }

      String core = readString(input);
      String disp = readString(input);
      boolean reseed = input.readBoolean();
      return new Seed(core, disp, reseed);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
