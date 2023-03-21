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

    switch (string) {
      case "blessed":
        return BUC.BLESSED;
      case "uncursed":
        return BUC.UNCURSED;
      case "cursed":
        return BUC.CURSED;
      default:
        return BUC.UNKNOWN;
    }
  }
}
