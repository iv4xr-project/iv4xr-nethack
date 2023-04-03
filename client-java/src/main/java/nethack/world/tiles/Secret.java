package nethack.world.tiles;

public interface Secret {
  boolean getIsSecret();

  default boolean isSecret() {
    return getIsSecret();
  }

  void setIsSecret(boolean isSecret);

  public default void setSecret() {
    setIsSecret(true);
  }
}
