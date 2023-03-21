package nethack.object.data;

public class Food {
  public String name;
  public Integer weight;
  public Integer cost;
  public Double nutrition;
  public Double nutritionPerWeight;
  public Double consumeTime;
  public Double nutritionPerConsumeTime;
  public FoodConduct foodConduct;

  private Food() {}

  public enum FoodConduct {
    MEAT,
    VEGETARIAN,
    VEGAN,
    NONE,
    VARIES;
  }
}
