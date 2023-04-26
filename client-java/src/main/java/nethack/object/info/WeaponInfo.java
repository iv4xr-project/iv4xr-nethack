package nethack.object.info;

import java.io.Serializable;

public class WeaponInfo implements Serializable {
  public String name;
  public String skill;
  public Integer cost;
  public Integer weight;
  public Integer nrHands;
  public Double damageToSmall;
  public Double damageToLarge;
  public String material;
  public String appearance;

  private WeaponInfo() {}

  public enum WeaponMaterial {
    IRON,
    WOOD,
    LEATHER,
    MINERAL,
    PLASTIC,
    METAL,
    SILVER,
    BONE,
    UNDEFINED;
  }
}
