package nethack.object.items;

import nethack.enums.BUC;
import nethack.enums.ItemType;
import nethack.object.data.Weapon;

public class WeaponItem extends Item implements BUCStatus, Weighable {
  public BUC buc;
  public Weapon weapon;
  public int modifier;

  public WeaponItem(
      char symbol,
      ItemType type,
      int glyph,
      String description,
      int quantity,
      BUC buc,
      Weapon weapon,
      int modifier) {
    super(symbol, type, glyph, description, quantity);
    this.buc = buc;
    this.weapon = weapon;
    this.modifier = modifier;
  }

  @Override
  public BUC getBUC() {
    return buc;
  }

  @Override
  public int getWeight() {
    return weapon.weight * quantity;
  }
}
