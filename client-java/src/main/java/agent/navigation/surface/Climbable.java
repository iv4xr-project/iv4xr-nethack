package agent.navigation.surface;

public interface Climbable {
  public enum ClimbType {
    Down,
    Up
  };

  public ClimbType getClimbType();
}
