package connection.messageencoder;

import connection.ConnectionLoggers;
import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Encoder {
  protected static final Logger logger = LogManager.getLogger(ConnectionLoggers.EncoderLogger);

  public static void sendString(DataOutputStream output, String msg) {
    try {
      output.writeShort(msg.length());
      output.writeChars(msg);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void sendInt(DataOutputStream output, int value) {
    try {
      output.writeInt(value);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void sendChar(DataOutputStream output, char character) {
    try {
      output.writeChar(character);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void writeByte(DataOutputStream output, int byteValue) {
    try {
      output.writeByte(byteValue);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public enum EncoderBit {
    ResetBit(1),
    SetSeedBit(2),
    GetSeedBit(3),
    RenderBit(4),
    CloseBit(5),
    StepBit(6),
    StepStrokeBit(7);
    public byte value = 0;

    private EncoderBit(int bitValue) {
      this.value = (byte) bitValue;
    }
  }
}