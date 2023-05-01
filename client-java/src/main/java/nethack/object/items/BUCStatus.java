package nethack.object.items;

import nethack.enums.BUC;

public interface BUCStatus {
  BUC getBUC();

  default boolean hasBUC() {
    return getBUC() != BUC.UNKNOWN;
  }
}
