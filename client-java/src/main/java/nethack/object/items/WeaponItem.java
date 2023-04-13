package nethack.object.items;

import nethack.enums.BUC;
import nethack.object.info.EntityInfo;
import nethack.object.info.WeaponInfo;

public class WeaponItem extends Item implements BUCStatus, Weighable {
  public BUC buc;
  public WeaponInfo weaponInfo;
  public int modifier;
  public boolean wielded;

  public WeaponItem(
      char symbol,
      EntityInfo entityInfo,
      int glyph,
      String description,
      int quantity,
      BUC buc,
      WeaponInfo weaponInfo,
      int modifier) {
    super(symbol, entityInfo, glyph, description, quantity);
    this.buc = buc;
    this.weaponInfo = weaponInfo;
    this.modifier = modifier;
    this.wielded =
        (description.contains("wielded") || description.contains("in hand"))
            && description.contains("not");
  }

  @Override
  public BUC getBUC() {
    return buc;
  }

  @Override
  public int getWeight() {
    return weaponInfo.weight * quantity;
  }
}
