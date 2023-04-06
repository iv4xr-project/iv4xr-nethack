package nethack.object;

import nethack.enums.Color;
import nethack.enums.SymbolType;

// Source: https://www.baeldung.com/java-enum-values
// Actions listed at: /python-server/lib/nle/nle/nethack/actions.py
public class Symbol {
  public final int glyph;
  public final Color color;
  public final char symbol;
  public final SymbolType type;

  public Symbol(int glyph, char symbol, SymbolType type, Color color) {
    this.glyph = glyph;
    this.color = color;
    this.symbol = symbol;
    this.type = type;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (obj instanceof Symbol) {
      Symbol other = (Symbol) obj;
      return glyph == other.glyph
          && symbol == other.symbol
          && type == other.type
          && other.color.equals(color);
    }
    return false;
  }
}
