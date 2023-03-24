package nethack.world.tiles;

public interface Viewable {
  public boolean isSeeThrough();

  public boolean isVisible();

  public void setVisible(boolean isVisible);
}
