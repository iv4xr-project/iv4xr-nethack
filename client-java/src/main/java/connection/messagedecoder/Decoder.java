package connection.messagedecoder;

import connection.ConnectionLoggers;
import java.io.DataInputStream;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Decoder {
  protected static final Logger logger = LogManager.getLogger(ConnectionLoggers.DecoderLogger);

  protected enum DecoderBit {
    ObservationBit(1),
    StepBit(2),
    SeedBit(3);
    public byte value;

    private DecoderBit(int value) {
      this.value = (byte) value;
    }
  }

  public static String readString(DataInputStream input) {
    try {
      int length = input.readShort();
      char[] chars = new char[length];

      for (int i = 0; i < length; i++) {
        chars[i] = input.readChar();
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
