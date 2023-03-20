package nethack.enums.appearance;

import java.util.HashMap;
import java.util.Map;

public enum WandAppearance {
  GLASS("glass"),
  BALSA("balsa"),
  CRYSTAL("crystal"),
  MAPLE("maple"),
  PINE("pine"),
  OAK("oak"),
  EBONY("ebony"),
  MARBLE("marble"),
  TIN("tin"),
  BRASS("brass"),
  COPPER("copper"),
  SILVER("silver"),
  PLATINUM("platinum"),
  IRIDIUM("iridium"),
  ZINC("zinc"),
  ALUMINUM("aluminum"),
  URANIUM("uranium"),
  IRON("iron"),
  STEEL("steel"),
  HEXAGONAL("hexagonal"),
  SHORT("short"),
  RUNED("runed"),
  LONG("long"),
  CURVED("curved"),
  FORKED("forked"),
  SPIKED("spiked"),
  JEWELED("jeweled");

  private final String value;

  private WandAppearance(String appearanceString) {
    value = appearanceString;
  }

  public static final Map<String, WandAppearance> mapping = new HashMap<>();

  static {
    for (WandAppearance wa : values()) {
      mapping.put(wa.value, wa);
    }
  }
}
