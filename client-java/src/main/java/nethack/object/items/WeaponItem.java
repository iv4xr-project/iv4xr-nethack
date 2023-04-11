// package nethack.object.items;
//
// import nethack.enums.BUC;
// import nethack.enums.EntityClass;
// import nethack.object.info.WeaponInfo;
//
// public class WeaponItem extends Item implements BUCStatus, Weighable {
//  public BUC buc;
//  public WeaponInfo weaponInfo;
//  public int modifier;
//
//  public WeaponItem(
//      char symbol,
//      EntityClass type,
//      int glyph,
//      String description,
//      int quantity,
//      BUC buc,
//      WeaponInfo weaponInfo,
//      int modifier) {
//    super(symbol, type, glyph, description, quantity);
//    this.buc = buc;
//    this.weaponInfo = weaponInfo;
//    this.modifier = modifier;
//  }
//
//  @Override
//  public BUC getBUC() {
//    return buc;
//  }
//
//  @Override
//  public int getWeight() {
//    return weaponInfo.weight * quantity;
//  }
// }
