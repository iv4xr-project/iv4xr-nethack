package nethack.object.info;

public class FoodInfo {
  public String name;
  public Integer weight;
  public Integer cost;
  public Double nutrition;
  public Double nutritionPerWeight;
  public Double consumeTime;
  public Double nutritionPerConsumeTime;
  public FoodConduct foodConduct;

  private FoodInfo() {}

  public enum FoodConduct {
    MEAT,
    VEGETARIAN,
    VEGAN,
    NONE,
    VARIES;
  }
}
