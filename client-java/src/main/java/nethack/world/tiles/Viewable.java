package nethack.world.tiles;

public interface Viewable {
  default boolean isVisible() {
    return getVisibility();
  }

  boolean getVisibility();

  void setVisibility(boolean isVisible);

  default void setVisible() {
    setVisibility(true);
  }

  default void resetVisibility() {
    setVisibility(false);
  }
}
