package agent.navigation.surface;

public interface Walkable {
  public default boolean isWalkable() {
    return getWalkableType() != null;
  }

  public WalkableType getWalkableType();

  public enum WalkableType {
    Straight,
    Diagonal,
    None,
  }
}
