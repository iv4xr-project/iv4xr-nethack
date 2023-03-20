package nethack.enums.appearance;

import java.util.HashMap;
import java.util.Map;

public enum RingAppearance {
  WOODEN("wooden"),
  GRANITE("granite"),
  OPAL("opal"),
  CLAY("clay"),
  CORAL("coral"),
  BLACK_ONYX("black onyx"),
  MOONSTONE("moonstone"),
  TIGER_EYE("tiger eye"),
  JADE("jade"),
  BRONZE("bronze"),
  AGATE("agate"),
  TOPAZ("topaz"),
  SAPPHIRE("sapphire"),
  RUBY("ruby"),
  DIAMOND("diamond"),
  PEARL("pearl"),
  IRON("iron"),
  BRASS("brass"),
  COPPER("copper"),
  TWISTED("twisted"),
  STEEL("steel"),
  SILVER("silver"),
  IVORY("ivory"),
  EMERALD("emerald"),
  WIRE("wire"),
  ENGAGEMENT("engagement"),
  SHINY("shiny");

  private final String value;

  private RingAppearance(String appearanceString) {
    value = appearanceString;
  }

  public static final Map<String, RingAppearance> mapping = new HashMap<>();

  static {
    for (RingAppearance ra : values()) {
      mapping.put(ra.value, ra);
    }
  }
}
