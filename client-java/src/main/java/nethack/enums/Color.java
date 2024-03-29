package nethack.enums;

import java.util.HashMap;
import java.util.Map;
import util.Config;

public enum Color {
  // Reference file: server-python/lib/nle/src/decl.c
  // Stack-overflow on terminal colors:
  // https://www.w3schools.blog/ansi-colors-java
  // Prefixes: https://invisible-island.net/xterm/ctlseqs/ctlseqs.html (Under: Character Attributes
  // (SGR))
  // Complete documentation:
  // https://www.ecma-international.org/wp-content/uploads/ECMA-48_5th_edition_june_1991.pdf
  // (8.3.117 SGR - Select graphic rendition)
  // Example color PNG:
  // https://github.com/remkop/picocli/commit/086bafdef1de14b320b03b34b5e5820597dd98aa
  BLACK(0, "0;30"),
  RED(1, "0;91"),
  GREEN(2, "0;32"),
  BROWN(3, "38;5;173"),
  BLUE(4, "0;34"),
  MAGENTA(5, "38;5;171"),
  CYAN(6, "38;5;123"),
  GRAY(7, ""),
  TRANSPARENT(8, "0;90"),
  ORANGE(9, "38;5;208"),
  GREEN_BRIGHT(10, "0;92"),
  YELLOW(11, "0;93"),
  BLUE_BRIGHT(12, "38;5;33"),
  MAGENTA_BRIGHT(13, "38;5;207"),
  CYAN_BRIGHT(14, "0;96"),
  WHITE(15, "0;97"),
  RESET(-1, "");

  private static final Map<Integer, Color> BY_VALUE = new HashMap<>();

  static {
    for (Color e : values()) {
      BY_VALUE.put(e.value, e);
    }
  }

  private final String colorCode;
  private final int value;

  Color(int value, String colorCode) {
    this.value = value;
    this.colorCode = colorCode;
  }

  public static Color fromValue(int value) {
    Color color = BY_VALUE.get(value);
    assert color != null : String.format("Color value '%d' not known", value);
    return color;
  }

  public static void main(String[] args) {
    for (Color color : Color.values()) {
      System.out.printf("%s %s%n", color, color.name());
    }

    System.out.printf("\033[103;32m W.O %s W.O", Color.GREEN);
  }

  public String toString() {
    // Ability to turn colors off for test exporting
    if (Config.getColorState()) {
      return String.format("\033[%sm", colorCode);
    } else {
      return "";
    }
  }
}
