package nethack.enums;

import java.io.Serializable;

// Source: server-python\lib\nle\include\objclass.h
public enum EntityClass implements Serializable {
  RANDOM,
  ILLOBJ, // Strange object
  WEAPON,
  ARMOR,
  RING,
  AMULET,
  TOOL,
  FOOD,
  POTION,
  SCROLL,
  SPELL_BOOK,
  WAND,
  COIN,
  GEM,
  ROCK,
  BALL,
  CHAIN,
  VENOM;

  public static int maxLength() {
    int maxLength = 0;
    for (EntityClass type : EntityClass.values()) {
      maxLength = Math.max(maxLength, type.name().length());
    }
    return maxLength;
  }
}
