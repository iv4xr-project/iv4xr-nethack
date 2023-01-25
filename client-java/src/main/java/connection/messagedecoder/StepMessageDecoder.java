package connection.messagedecoder;

import connection.StepMessage;
import java.io.DataInputStream;
import java.io.IOException;

public class StepMessageDecoder extends Decoder {
  public static StepMessage decode(DataInputStream input) {
    try {
      StepMessage stepMessage = new StepMessage();
      if (input.readByte() != DecoderBit.StepBit.value) {
        logger.fatal("Did not receive step message byte");
        System.exit(-1);
      }
      stepMessage.done = input.readBoolean();
      return stepMessage;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
