package nethack.object;

import nethack.enums.Color;
import nethack.enums.SymbolType;
import util.CustomVec2D;

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

  public String createId(CustomVec2D pos) {
    return String.format("%s_%d", type.name(), glyph /*, id*/);
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
          && other.color.equals(color)
          && type == other.type;
    }
    return false;
  }
}
