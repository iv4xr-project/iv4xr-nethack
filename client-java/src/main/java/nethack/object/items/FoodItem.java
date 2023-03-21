package nethack.object.items;

import nethack.enums.BUC;
import nethack.enums.ItemType;
import nethack.object.data.Food;

public class FoodItem extends Item implements BUCStatus, Weighable {
  public BUC buc;
  public Food food;

  public FoodItem(
      char symbol, ItemType type, int glyph, String description, int quantity, BUC buc, Food food) {
    super(symbol, type, glyph, description, quantity);
    this.buc = buc;
    this.food = food;
  }

  @Override
  public BUC getBUC() {
    return buc;
  }

  @Override
  public int getWeight() {
    return food.weight * quantity;
  }
}
