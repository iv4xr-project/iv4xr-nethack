package connection.messagedecoder;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import nethack.enums.BUC;
import nethack.enums.ItemType;
import nethack.object.Item;

public class ItemDecoder extends Decoder {
  public static Item decode(DataInputStream input) {
    try {
      char symbol = input.readChar();
      int itemClass = input.readByte();
      int itemGlyph = input.readShort();
      byte[] chars = input.readNBytes(80);

      // Interpret values
      ItemType type = ItemType.values()[itemClass];
      String description = new String(chars, StandardCharsets.UTF_8);
      description = description.replaceAll("\0", "");
      String[] words = description.split(" ");
      int quantity = words[0].matches("\\d+") ? Integer.parseInt(words[0]) : 1;

      BUC buc;
      switch (words[1]) {
        case "blessed":
          buc = BUC.BLESSED;
          break;
        case "uncursed":
          buc = BUC.UNCURSED;
          break;
        case "cursed":
          buc = BUC.CURSED;
          break;
        default:
          buc = BUC.UNKNOWN;
          break;
      }

      return new Item(symbol, type, itemGlyph, description, buc, quantity);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
