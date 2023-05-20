package connection.messageencoder;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;
import nethack.object.Seed;

public class SeedEncoder {
  public static void encode(DataOutputStream output, Seed seed) {
    try {
      if (!Objects.equals(seed.core, "")) {
        output.writeShort(seed.core.length());
        output.writeChars(seed.core);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
