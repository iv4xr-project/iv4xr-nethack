package connection.messagedecoder;

import java.io.DataInputStream;
import java.io.IOException;
import nethack.object.Seed;

public class SeedDecoder extends Decoder {
  public static Seed decode(DataInputStream input) {
    try {
      int inputByte = input.readByte();
      assert inputByte == DecoderBit.SeedBit.value;

      String core = readString(input);
      return new Seed(core);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
