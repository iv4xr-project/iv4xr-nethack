package connection.messagedecoder;

import java.io.DataInputStream;
import java.io.IOException;
import nethack.enums.Color;
import nethack.enums.EntityType;
import nethack.object.Entity;

// Source: https://studytrails.com/2016/09/12/java-google-json-type-adapter/
public class EntityDecoder extends Decoder {
  public static Entity decode(DataInputStream input) {
    try {
      long now = System.nanoTime();
      boolean verbose = false;
      char symbol = input.readChar();
      int colorCode = input.readByte();
      int glyph = input.readShort();

      long now1 = System.nanoTime();
      if (verbose) System.out.printf("READ STUFF TOOK: %d%n", now1 - now);

      Color color = Color.fromValue(colorCode);
      EntityType type = toEntityType(glyph, symbol, color);
      long maps = System.nanoTime();
      if (verbose)
        System.out.printf(
            "READ Interpret COLOR+TYPE OF (%s, %s) TOOK: %d%n", color, type, maps - now1);
      Entity e = new Entity(glyph, symbol, type, color);
      long now2 = System.nanoTime();
      if (verbose) System.out.printf("CREATING ENTITY TOOK: %d%n", now2 - maps);
      return e;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static EntityType toEntityType(int glyph, char symbol, Color color) {
    EntityType type = toEntityType(glyph);
    if (type != EntityType.UNKNOWN) {
      return type;
    }

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
      case 5913: // White :
        return EntityType.STATUE;
      case 2389:
        return EntityType.SINK;
      case 2380:
        return EntityType.CORRIDOR;
      case 2379:
      case 2378:
        return EntityType.FLOOR;
      case 2371:
        return EntityType.DOORWAY;
      case 2359:
        return EntityType.VOID;
      case 397: // Pet dog
        return EntityType.PET;
      case 16: // Same character as pet dog, but not the PET
        return EntityType.MONSTER;
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
        return color == Color.CYAN ? EntityType.PRISON_BARS : EntityType.UNKNOWN;
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
}
