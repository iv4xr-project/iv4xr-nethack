package nethack.object.items;

import nethack.enums.BUC;

public interface BUCStatus {
  public BUC getBUC();

  public default boolean hasBUC() {
    return getBUC() != BUC.UNKNOWN;
  }
}
