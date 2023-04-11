package connection.messagedecoder;

import java.io.DataInputStream;
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
      int length = input.readByte();

      // String with no length
      if (length == 0) {
        return "";
      }

      return readString(input, length);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String readString(DataInputStream input, int length) {
    try {
      char[] chars = new char[length];
      byte[] bytes = input.readNBytes(length);

      for (int i = 0; i < length; i++) {
        chars[i] = parseChar(bytes[i]);
      }

      return String.valueOf(chars);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static int parseInt(byte b1, byte b2, byte b3, byte b4) {
    int i1 = toPositiveInt(b1);
    int i2 = toPositiveInt(b2);
    int i3 = toPositiveInt(b3);
    int i4 = toPositiveInt(b4);
    return (i1 << 24) + (i2 << 16) + (i3 << 8) + i4;
  }

  public static short parseShort(byte b1, byte b2) {
    int i1 = toPositiveInt(b1);
    int i2 = toPositiveInt(b2);
    return (short) ((i1 << 8) + i2);
  }

  public static boolean parseBool(byte b) {
    return b == 1;
  }

  public static char parseChar(byte b) {
    return (char) b;
  }

  private static int toPositiveInt(byte b) {
    if (b >= 0) {
      return b;
    }
    return b & 0xff;
  }

  public static byte readByte(DataInputStream input) {
    try {
      return input.readByte();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
