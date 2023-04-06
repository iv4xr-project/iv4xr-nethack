package connection.messagedecoder;

import java.io.DataInputStream;
import java.io.IOException;
import nethack.enums.Color;
import nethack.enums.SymbolType;
import nethack.object.Symbol;
import nethack.world.Level;
import util.Loggers;

// Source: https://studytrails.com/2016/09/12/java-google-json-type-adapter/
public class SymbolDecoder extends Decoder {
  public static Symbol[][] decode(DataInputStream input) throws IOException {
    Symbol[][] symbols = new Symbol[Level.SIZE.height][Level.SIZE.width];

    int bytesPerEntity = 6;
    int nrSymbols = input.readShort();
    byte[] symbolsData = input.readNBytes(bytesPerEntity * nrSymbols);
    for (int i = 0, offset = 0; i < nrSymbols; i++, offset += bytesPerEntity) {
      byte x = symbolsData[offset];
      byte y = symbolsData[offset + 1];
      char symbol = parseChar(symbolsData[offset + 2]);
      byte colorCode = symbolsData[offset + 3];
      int glyph = parseShort(symbolsData[offset + 4], symbolsData[offset + 5]);
      symbols[y][x] = toSymbol(symbol, colorCode, glyph);
    }

    return symbols;
  }

  private static Symbol toSymbol(char symbol, int colorCode, int glyph) {
    Color color = Color.fromValue(colorCode);
    SymbolType type = toSymbolType(glyph, symbol, color);
    return new Symbol(glyph, symbol, type, color);
  }

  private static SymbolType toSymbolType(int glyph, char symbol, Color color) {
    SymbolType type = toSymbolType(glyph);
    if (type != SymbolType.UNKNOWN) {
      return type;
    }

    if (symbol == '@') {
      Loggers.EncoderLogger.warn("%s%s%s: %d", color, symbol, Color.RESET, glyph);
    }

    type = toSymbolType(symbol, color);
    if (type != SymbolType.UNKNOWN) {
      return type;
    }

    Loggers.EncoderLogger.warn("%s%s%s: %d UNKNOWN", color, symbol, Color.RESET, glyph);
    return SymbolType.UNKNOWN;
  }

  private static SymbolType toSymbolType(int glyph) {
    switch (glyph) {
      case 2389:
        return SymbolType.SINK;
      case 2381:
      case 2380:
        return SymbolType.CORRIDOR;
      case 2379:
      case 2378:
        return SymbolType.FLOOR;
      case 2377:
        return SymbolType.TREE;
      case 2374: // Door in vertical wall
      case 2375: // Door in horizontal wall
        return SymbolType.DOOR;
      case 2371:
        return SymbolType.DOORWAY;
      case 2359:
        return SymbolType.VOID;
      case 333: // Player
        return SymbolType.PLAYER;
      case 267:
        return SymbolType.SHOPKEEPER;
      case 16: // Same character as pet dog, but not the PET
        return SymbolType.MONSTER;
    }

    return SymbolType.UNKNOWN;
  }

  private static SymbolType toSymbolType(char symbol, Color color) {
    // When simply the symbol and color is enough to identify the type
    switch (symbol) {
      case '_':
        return SymbolType.ALTAR;
      case '"':
        return SymbolType.AMULET;
      case ']':
        return SymbolType.STRANGE_OBJECT;
      case '[':
        return SymbolType.ARMOR;
      case '0':
        return SymbolType.BALL;
      case '`':
        return SymbolType.BOULDER;
      case '#':
        return color == Color.CYAN ? SymbolType.PRISON_BARS : SymbolType.UNKNOWN;
      case '+':
        return SymbolType.SPELLBOOK;
        // DOORWAY;
      case '%':
        return SymbolType.EDIBLE;
      case '.':
        return color == Color.BLUE_BRIGHT ? SymbolType.ICE : SymbolType.FLOOR;
      case '{':
        return SymbolType.FOUNTAIN;
        // GEM/ROCK
      case '|':
      case '-':
        if (color == Color.BROWN) return SymbolType.DOOR;
        if (color == Color.WHITE) return SymbolType.GRAVE;
        return SymbolType.WALL;
      case '$':
        return SymbolType.GOLD;
      case '@':
        return SymbolType.HUMAN;
      case '(':
        return SymbolType.ITEM;
      case '~':
        return color == Color.RED ? SymbolType.LAVA : SymbolType.WATER;
      case '!':
        return SymbolType.POTION;
      case '=':
        return SymbolType.RING;
      case '?':
        return SymbolType.SCROLL;
      case '>':
        return SymbolType.STAIRS_DOWN;
      case '<':
        return SymbolType.STAIRS_UP;
        // SPIDERWEB
      case '\\':
        return SymbolType.THRONE;
      case '^':
        return color == Color.MAGENTA ? SymbolType.PORTAL : SymbolType.TRAP;
      case ' ':
        return SymbolType.VOID;
      case '/':
        return SymbolType.WAND;
      case ')':
        return SymbolType.WEAPON;
      case '*':
        return SymbolType.ROCK;
    }

    if (symbol == 'I' && color == Color.TRANSPARENT) {
      return SymbolType.MONSTER;
    }

    //    if (Character.isAlphabetic(symbol) || symbol == ':' || symbol == '\'') {
    //      return id == 0 ? EntityType.STATUE : EntityType.MONSTER;
    //    }

    return SymbolType.UNKNOWN;
  }
}
