package nethack.enums.appearance;

import java.util.HashMap;
import java.util.Map;

public enum SpellbookAppearance {
  PARCHMENT("parchment"),
  VELLUM("vellum"),
  RAGGED("ragged"),
  DOG_EARED("dog eared"),
  MOTTLED("mottled"),
  STAINED("stained"),
  CLOTH("cloth"),
  LEATHERY("leathery"),
  WHITE("white"),
  PINK("pink"),
  RED("red"),
  ORANGE("orange"),
  YELLOW("yellow"),
  VELVET("velvet"),
  LIGHT_GREEN("light green"),
  DARK_GREEN("dark green"),
  TURQUOISE("turquoise"),
  CYAN("cyan"),
  LIGHT_BLUE("light blue"),
  DARK_BLUE("dark blue"),
  INDIGO("indigo"),
  MAGENTA("magenta"),
  PURPLE("purple"),
  VIOLET("violet"),
  TAN("tan"),
  PLAID("plaid"),
  LIGHT_BROWN("light brown"),
  DARK_BROWN("dark brown"),
  GRAY("gray"),
  WRINKLED("wrinkled"),
  DUSTY("dusty"),
  BRONZE("bronze"),
  COPPER("copper"),
  SILVER("silver"),
  GOLD("gold"),
  GLITTERING("glittering"),
  SHINING("shining"),
  DULL("dull"),
  THIN("thin"),
  THICK("thick");

  private final String value;

  SpellbookAppearance(String appearanceString) {
    value = appearanceString;
  }

  public static final Map<String, SpellbookAppearance> mapping = new HashMap<>();

  static {
    for (SpellbookAppearance sa : values()) {
      mapping.put(sa.value, sa);
    }
  }
}
