package connection.messagedecoder;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import nethack.enums.ItemType;
import nethack.object.Item;

public class ItemDecoder extends Decoder {
  public static Item decode(DataInputStream input) {
    try {
      char symbol = input.readChar();
      int itemClass = input.readByte();
      byte[] chars = input.readNBytes(80);
      String description = new String(chars, StandardCharsets.UTF_8);
      description = description.replaceAll("\0", "");
      ItemType type = ItemType.values()[itemClass];

      return new Item(symbol, type, description);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
