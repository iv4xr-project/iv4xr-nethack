package nethack.enums;

public enum BUC {
  BLESSED,
  UNCURSED,
  CURSED,
  UNKNOWN;

  public static BUC fromString(String string) {
    if (string == null) {
      return BUC.UNKNOWN;
    }

    return switch (string) {
      case "blessed" -> BUC.BLESSED;
      case "uncursed" -> BUC.UNCURSED;
      case "cursed" -> BUC.CURSED;
      default -> BUC.UNKNOWN;
    };
  }
}
