package connection.messageencoder;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Encoder {
  public static void writeString(DataOutputStream output, String msg) {
    try {
      output.writeShort(msg.length());
      output.writeChars(msg);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void writeInt(DataOutputStream output, int value) {
    try {
      output.writeInt(value);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void writeChar(DataOutputStream output, char character) {
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

  public static void writeBoolean(DataOutputStream output, boolean booleanValue) {
    if (booleanValue) {
      writeByte(output, 1);
    } else {
      writeByte(output, 0);
    }
  }

  public enum EncoderBit {
    ResetBit(1),
    SetSeedBit(2),
    GetSeedBit(3),
    RenderBit(4),
    CloseBit(5),
    StepBit(6),
    SaveCoverage(7),
    ResetCoverage(8);
    public byte value = 0;

    private EncoderBit(int bitValue) {
      this.value = (byte) bitValue;
    }
  }
}
