package nethack.object.items;

import nethack.enums.BUC;
import nethack.object.info.EntityInfo;
import nethack.object.info.FoodInfo;

public class FoodItem extends Item implements BUCStatus, Weighable {
  public BUC buc;
  public FoodInfo foodInfo;

  public FoodItem(
      char symbol,
      EntityInfo entityInfo,
      int glyph,
      String description,
      int quantity,
      BUC buc,
      FoodInfo foodInfo) {
    super(symbol, entityInfo, glyph, description, quantity);
    this.buc = buc;
    this.foodInfo = foodInfo;
  }

  @Override
  public BUC getBUC() {
    return buc;
  }

  @Override
  public int getWeight() {
    return foodInfo.weight * quantity;
  }
}
