package connection.messagedecoder;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nethack.enums.BUC;
import nethack.enums.EntityClass;
import nethack.object.info.EntityInfo;
import nethack.object.items.FoodItem;
import nethack.object.items.Item;
import nethack.object.items.WeaponItem;
import util.Database;

public class ItemDecoder extends Decoder {
  static final Pattern p =
      Pattern.compile(
          "^(?<quantity>\\w+)\\s(?:(?<buc>uncursed|cursed|blessed)\\s)?(?:partly"
              + " eaten\\s)?(?:(?<modifier>[\\+-]\\d+)\\s)?(?:(?<name>[^(]*))(?:\\s\\((?<additional>.*)\\))?$");

  public static Item[] decode(DataInputStream input) throws IOException {
    int nr_items = input.readByte();
    Item[] items = new Item[nr_items];

    for (int i = 0; i < nr_items; i++) {
      items[i] = toItem(input);
    }

    return items;
  }

  private static Item toItem(DataInputStream input) {
    try {
      char symbol = parseChar(input.readByte());
      int itemGlyph = input.readShort();

      // Interpret values
      String description = readString(input, 80);
      description = description.replaceAll("\0", "");

      Map<String, Object> info = descriptionInterpreter(description);
      int quantity = (int) info.get("quantity");
      BUC buc = (BUC) info.get("buc");
      int modifier = (int) info.get("modifier");
      //      String name = (String) info.get("name");
      String additional = (String) info.get("additional");
      EntityInfo entityInfo = Database.getEntityInfoFromGlyph(itemGlyph);

      if (entityInfo.entityClass == EntityClass.FOOD) {
        return new FoodItem(
            symbol,
            entityInfo,
            itemGlyph,
            description,
            quantity,
            buc,
            Database.getFood(entityInfo.name));
      } else if (entityInfo.entityClass == EntityClass.WEAPON) {
        return new WeaponItem(
            symbol,
            entityInfo,
            itemGlyph,
            description,
            quantity,
            buc,
            Database.getWeapon(entityInfo.name),
            modifier);
      }

      return new Item(symbol, entityInfo, itemGlyph, description, quantity);
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
