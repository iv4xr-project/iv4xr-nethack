package connection.messageparser;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import connection.ConnectionLoggers;
import java.io.IOException;
import nethack.enums.Color;
import nethack.enums.EntityType;
import nethack.object.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Source: https://studytrails.com/2016/09/12/java-google-json-type-adapter/
public class EntityTypeAdapter extends TypeAdapter<Entity> {
  static final Logger logger = LogManager.getLogger(ConnectionLoggers.TypeAdapterLogger);

  private static EntityType toEntityType(int glyph, char symbol, Color color) {
    EntityType type = toEntityType(glyph);
    if (type != EntityType.UNKNOWN) {
      return type;
    }
    //        logger.warn(String.format("%s%s%s: %d UNKNOWN", color.stringCode(), symbol,
    // Color.RESET.stringCode(), glyph));

    type = toEntityType(symbol, color);
    if (type != EntityType.UNKNOWN) {
      return type;
    }

    logger.warn(
        String.format(
            "%s%s%s: %d UNKNOWN", color.stringCode(), symbol, Color.RESET.stringCode(), glyph));
    return EntityType.UNKNOWN;
  }

  private static EntityType toEntityType(int glyph) {
    switch (glyph) {
      case 2359:
        return EntityType.VOID;
      case 2379:
      case 2378:
        return EntityType.FLOOR;
      case 2371:
        return EntityType.DOORWAY;
    }

    return EntityType.UNKNOWN;
  }

  private static EntityType toEntityType(char symbol, Color color) {
    // When simply the symbol and color is enough to identify the type
    switch (symbol) {
      case '_':
        return EntityType.ALTAR;
      case '"':
        return EntityType.AMULET;
      case '[':
        return EntityType.ARMOR;
      case '0':
        return EntityType.BALL;
      case '`':
        return EntityType.BOULDER;
      case '#':
        return color == Color.CYAN ? EntityType.PRISON_BARS : EntityType.CORRIDOR;
      case '+':
        return color == Color.BROWN ? EntityType.DOOR : EntityType.SPELLBOOK;
        // DOORWAY;
      case '%':
        return EntityType.EDIBLE;
      case '.':
        return color == Color.BLUE_BRIGHT ? EntityType.ICE : EntityType.FLOOR;
      case '{':
        return EntityType.FOUNTAIN;
        // GEM/ROCK
      case '|':
      case '-':
        if (color == Color.BROWN) return EntityType.DOOR;
        if (color == Color.WHITE) return EntityType.GRAVE;
        return EntityType.WALL;
      case '$':
        return EntityType.GOLD;
      case '@':
        return color == Color.WHITE ? EntityType.PLAYER : EntityType.HUMAN;
      case '(':
        return EntityType.ITEM;
      case 'I':
        return color == Color.TRANSPARENT ? EntityType.LAST_LOCATION : EntityType.MONSTER;
      case 'd':
      case 'f':
        return color == Color.WHITE ? EntityType.PET : EntityType.MONSTER;
      case 'u':
        return color == Color.BROWN ? EntityType.PET : EntityType.MONSTER;
      case '}':
        return EntityType.POOL;
      case '!':
        return EntityType.POTION;
      case '=':
        return EntityType.RING;
      case '?':
        return EntityType.SCROLL;
      case '>':
        return EntityType.STAIRS_DOWN;
      case '<':
        return EntityType.STAIRS_UP;
        // SPIDERWEB
      case '\\':
        return EntityType.THRONE;
      case '^':
        return color == Color.MAGENTA ? EntityType.PORTAL : EntityType.TRAP;
      case ' ':
        return EntityType.VOID;
      case '/':
        return EntityType.WAND;
      case ')':
        return EntityType.WEAPON;
    }

    if (Character.isAlphabetic(symbol) || symbol == ':') {
      if (color == Color.WHITE) {
        return EntityType.STATUE;
      }
      return EntityType.MONSTER;
    }

    return EntityType.UNKNOWN;
  }

  @Override
  public Entity read(JsonReader reader) throws IOException {
    // the first token is the start array
    JsonToken token = reader.peek();
    if (token.equals(JsonToken.BEGIN_ARRAY)) {
      reader.beginArray();
      int glyph = (int) reader.nextInt();
      char symbol = (char) reader.nextInt();
      Color color = new ColorTypeAdapter().read(reader);
      reader.endArray();

      // Infer Entity type
      EntityType type = toEntityType(glyph, symbol, color);
      return new Entity(glyph, symbol, type, color);
    } else {
      logger.warn("Tried to read an entity, however it was not in an array");
      return null;
    }
  }

  @Override
  public void write(JsonWriter out, Entity entity) throws IOException {}
}
