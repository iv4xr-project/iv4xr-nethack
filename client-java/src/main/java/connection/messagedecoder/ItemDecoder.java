package connection.messagedecoder;

import java.io.DataInputStream;
import java.io.IOException;
import nethack.enums.ItemType;
import nethack.object.Item;

public class ItemDecoder extends Decoder {
  public static Item decode(DataInputStream input) {
    try {
      char symbol = input.readChar();
      int itemClass = input.readByte();
      String description = readString(input);
      ItemType type = ItemType.values()[itemClass];

      return new Item(symbol, type, description);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
