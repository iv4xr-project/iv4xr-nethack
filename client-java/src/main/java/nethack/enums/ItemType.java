package nethack.enums;

// Source: server-python\lib\nle\include\objclass.h
public enum ItemType {
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
  SPBOOK,
  WAND,
  COIN,
  GEM,
  ROCK,
  BALL,
  CHAIN,
  VENOM,
  NONE;

  public static int maxLength() {
    int maxLength = 0;
    for (ItemType type : ItemType.values()) {
      maxLength = Math.max(maxLength, type.name().length());
    }
    return maxLength;
  }
}
