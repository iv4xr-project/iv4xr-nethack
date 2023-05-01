package agent.navigation.surface;

public interface Climbable {
  enum ClimbType {
    Down,
    Up
  }

  ClimbType getClimbType();
}
