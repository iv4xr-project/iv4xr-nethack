package nethack.world.tiles;

public interface Viewable {
  public boolean isSeeThrough();

  public default boolean isVisible() {
    return getVisibility();
  }

  public boolean getVisibility();

  public void setVisibility(boolean isVisible);

  public default void setVisible() {
    setVisibility(true);
  }

  public default void resetVisibility() {
    setVisibility(false);
  }
}
