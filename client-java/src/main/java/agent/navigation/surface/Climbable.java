package agent.navigation.surface;

public interface Climbable {
  public enum ClimbType {
    Descendable,
    Ascendable
  };

  public ClimbType getClimbType();

  public Climbable getOtherSide();
}
