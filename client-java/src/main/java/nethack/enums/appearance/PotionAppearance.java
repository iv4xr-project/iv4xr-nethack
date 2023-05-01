package nethack.enums.appearance;

import java.util.HashMap;
import java.util.Map;

public enum PotionAppearance {
  RUBY("ruby"),
  PINK("pink"),
  ORANGE("orange"),
  YELLOW("yellow"),
  EMERALD("emerald"),
  DARK_GREEN("dark green"),
  CYAN("cyan"),
  SKY_BLUE("sky blue"),
  BRILLIANT_BLUE("brilliant blue"),
  MAGENTA("magenta"),
  PURPLE_RED("purple-red"),
  PUCE("puce"),
  MILKY("milky"),
  SWIRLY("swirly"),
  BUBBLY("bubbly"),
  SMOKY("smoky"),
  CLOUDY("cloudy"),
  EFFERVESCENT("effervescent"),
  BLACK("black"),
  GOLDEN("golden"),
  BROWN("brown"),
  FIZZY("fizzy"),
  DARK("dark"),
  WHITE("white"),
  MURKY("murky"),
  CLEAR("clear");

  private final String value;

  PotionAppearance(String appearanceString) {
    value = appearanceString;
  }

  public static final Map<String, PotionAppearance> mapping = new HashMap<>();

  static {
    for (PotionAppearance pa : values()) {
      mapping.put(pa.value, pa);
    }
  }
}
