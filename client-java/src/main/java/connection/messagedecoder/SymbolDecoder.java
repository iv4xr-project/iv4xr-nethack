package connection.messagedecoder;

import java.io.DataInputStream;
import java.io.IOException;
import nethack.enums.Color;
import nethack.object.Symbol;
import nethack.world.Level;

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
    return new Symbol(glyph, symbol, color);
  }
}
