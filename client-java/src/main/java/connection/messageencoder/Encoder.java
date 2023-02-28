package connection.messageencoder;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Encoder {
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
    StepStrokeBit(7),
    SaveCoverage(8),
    ResetCoverage(9);
    public byte value = 0;

    private EncoderBit(int bitValue) {
      this.value = (byte) bitValue;
    }
  }
}
