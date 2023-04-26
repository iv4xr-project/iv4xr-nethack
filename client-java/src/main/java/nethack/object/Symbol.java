package nethack.object;

import java.io.Serializable;
import nethack.enums.Color;

// Source: https://www.baeldung.com/java-enum-values
// Actions listed at: /python-server/lib/nle/nle/nethack/actions.py
public class Symbol implements Serializable {
  public final int glyph;
  public final Color color;
  public final char symbol;

  public Symbol(int glyph, char symbol, Color color) {
    this.glyph = glyph;
    this.color = color;
    this.symbol = symbol;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (obj instanceof Symbol) {
      Symbol other = (Symbol) obj;
      return glyph == other.glyph && symbol == other.symbol && other.color.equals(color);
    }
    return false;
  }
}
