package connection.messagedecoder;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

public abstract class Decoder {
  protected enum DecoderBit {
    ObservationBit(1),
    StepBit(2),
    SeedBit(3);
    public final byte value;

    private DecoderBit(int value) {
      this.value = (byte) value;
    }
  }

  public static String readString(DataInputStream input) {
    try {
      int length = input.readShort();

      // String with no length
      if (length == 0) {
        return "";
      }

      char[] chars = new char[length];
      byte[] bytes = input.readNBytes(length * 2);

      for (int i = 0; i < length; i++) {
        int offset = i * 2;

        int ch1 = bytes[offset];
        int ch2 = bytes[offset + 1];
        if ((ch1 | ch2) < 0) throw new EOFException();
        chars[i] = (char) ((ch1 << 8) + ch2);
      }

      return String.valueOf(chars);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static byte readByte(DataInputStream input) {
    try {
      return input.readByte();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
