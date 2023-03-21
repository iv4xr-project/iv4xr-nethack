package connection.messagedecoder;

import java.io.DataInputStream;
import nethack.enums.Color;
import nethack.enums.EntityType;
import nethack.object.Entity;
import util.Loggers;

// Source: https://studytrails.com/2016/09/12/java-google-json-type-adapter/
public class EntityDecoder extends Decoder {
  public static Entity decode(
      DataInputStream input, char symbol, int colorCode, int glyph, int id) {
    Color color = Color.fromValue(colorCode);
    EntityType type = toEntityType(glyph, symbol, id, color);
    return new Entity(glyph, symbol, id, type, color);
  }

  private static EntityType toEntityType(int glyph, char symbol, int id, Color color) {
    EntityType type = toEntityType(glyph);
    if (type != EntityType.UNKNOWN) {
      return type;
    }

    if (symbol == '@') {
      Loggers.EncoderLogger.warn("%s%s%s: %d", color, symbol, Color.RESET, glyph);
    }

    type = toEntityType(symbol, id, color);
    if (type != EntityType.UNKNOWN) {
      return type;
    }

    Loggers.EncoderLogger.warn("%s%s%s: %d UNKNOWN", color, symbol, Color.RESET, glyph);
    return EntityType.UNKNOWN;
  }

  private static EntityType toEntityType(int glyph) {
    switch (glyph) {
        //      case 5913: // White :
        //        return EntityType.STATUE;
      case 2389:
        return EntityType.SINK;
      case 2381:
      case 2380:
        return EntityType.CORRIDOR;
      case 2379:
      case 2378:
        return EntityType.FLOOR;
      case 2377:
        return EntityType.TREE;
      case 2374: // Door in vertical wall
      case 2375: // Door in horizontal wall
        return EntityType.DOOR;
      case 2371:
        return EntityType.DOORWAY;
      case 2359:
        return EntityType.VOID;
      case 397: // Pet dog
        return EntityType.PET;
      case 333: // Player
        return EntityType.PLAYER;
      case 267:
        return EntityType.SHOPKEEPER;
      case 16: // Same character as pet dog, but not the PET
        return EntityType.MONSTER;
    }

    return EntityType.UNKNOWN;
  }

  private static EntityType toEntityType(char symbol, int id, Color color) {
    // When simply the symbol and color is enough to identify the type
    switch (symbol) {
      case '_':
        return EntityType.ALTAR;
      case '"':
        return EntityType.AMULET;
      case ']':
        return EntityType.STRANGE_OBJECT;
      case '[':
        return EntityType.ARMOR;
      case '0':
        return EntityType.BALL;
      case '`':
        return EntityType.BOULDER;
      case '#':
        return color == Color.CYAN ? EntityType.PRISON_BARS : EntityType.UNKNOWN;
      case '+':
        return EntityType.SPELLBOOK;
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
        return EntityType.HUMAN;
      case '(':
        return EntityType.ITEM;
      case 'f':
        return color == Color.WHITE ? EntityType.PET : EntityType.MONSTER;
      case 'u':
        return color == Color.BROWN ? EntityType.PET : EntityType.MONSTER;
      case '~':
        return color == Color.RED ? EntityType.LAVA : EntityType.WATER;
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
      case '*':
        return EntityType.ROCK;
    }

    if (symbol == 'I' && color == Color.TRANSPARENT) {
      return EntityType.MONSTER;
    }

    if (Character.isAlphabetic(symbol) || symbol == ':' || symbol == '\'') {
      return id == 0 ? EntityType.STATUE : EntityType.MONSTER;
    }

    return EntityType.UNKNOWN;
  }
}
