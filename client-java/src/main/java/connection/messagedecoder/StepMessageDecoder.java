package connection.messagedecoder;

import connection.StepMessage;
import java.io.DataInputStream;
import java.io.IOException;

public class StepMessageDecoder extends Decoder {
  public static StepMessage decode(DataInputStream input) {
    try {
      StepMessage stepMessage = new StepMessage();
      int inputByte = input.readByte();
      assert inputByte == DecoderBit.StepBit.value;
      stepMessage.done = input.readBoolean();
      return stepMessage;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
