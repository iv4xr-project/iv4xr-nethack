package agent.navigation.surface;

import nethack.enums.Color;

public interface Printable extends Viewable {
  public char toChar();

  public default String toColoredString() {
    if (!isVisible()) {
      return String.valueOf(toChar());
    }

    return Color.GREEN_BRIGHT.stringCode() + toChar() + Color.RESET.stringCode();
  }
}
