package connection.messagedecoder;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nethack.enums.BUC;
import nethack.enums.ItemType;
import nethack.object.items.FoodItem;
import nethack.object.items.Item;
import nethack.object.items.WeaponItem;
import util.Database;

public class ItemDecoder extends Decoder {
  static final Pattern p =
      Pattern.compile(
          "^(?<quantity>\\w+)\\s(?:(?<buc>uncursed|cursed|blessed)\\s)?(?:(?<modifier>[\\+-]\\d+)\\s)?(?:(?<name>[^(]*))(?:\\s\\((?<additional>.*)\\))?$");

  public static Item decode(DataInputStream input) {
    try {
      char symbol = input.readChar();
      int itemClass = input.readByte();
      int itemGlyph = input.readShort();
      byte[] chars = input.readNBytes(80);

      // Interpret values
      ItemType type = ItemType.values()[itemClass];
      String description = new String(chars, StandardCharsets.UTF_8).replaceAll("\0", "");

      Map<String, Object> info = descriptionInterpreter(description);
      int quantity = (int) info.get("quantity");
      BUC buc = (BUC) info.get("buc");
      int modifier = (int) info.get("modifier");
      String name = (String) info.get("name");
      String additional = (String) info.get("additional");

      if (type == ItemType.FOOD) {
        return new FoodItem(
            symbol, type, itemGlyph, description, quantity, buc, Database.getFood(name));
      } else if (type == ItemType.WEAPON) {
        return new WeaponItem(
            symbol,
            type,
            itemGlyph,
            description,
            quantity,
            buc,
            Database.getWeapon(name),
            modifier);
      }

      return new Item(symbol, type, itemGlyph, description, quantity);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Map<String, Object> descriptionInterpreter(String description) {
    Map<String, Object> mapping = new HashMap<>();

    Matcher matcher = p.matcher(description);
    boolean matches = matcher.matches();
    assert matches : String.format("Match does not work on: \"%s\"", description);

    String quantityString = matcher.group("quantity");
    int quantity = quantityString.matches("\\d+") ? Integer.parseInt(quantityString) : 1;
    mapping.put("quantity", quantity);

    mapping.put("buc", BUC.fromString(matcher.group("buc")));

    String modifierString = matcher.group("modifier");
    int modifier = modifierString == null ? 0 : Integer.parseInt(modifierString);
    mapping.put("modifier", modifier);

    mapping.put("name", matcher.group("name"));
    mapping.put("additional", matcher.group("additional"));

    return mapping;
  }
}
