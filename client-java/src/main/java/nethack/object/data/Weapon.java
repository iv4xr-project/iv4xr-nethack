package nethack.object.data;

public class Weapon {
  public String name;
  public String skill;
  public Integer cost;
  public Integer weight;
  public Integer nrHands;
  public Double damageToSmall;
  public Double damageToLarge;
  public String material;
  public String appearance;

  private Weapon() {}

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
