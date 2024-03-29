package nethack.enums;

public enum GameMode {
  NetHackChallenge("NetHackChallenge-v0"), // Does not allow seeding
  NetHack("NetHack-v0"); // Allows for seeding

  private final String environmentName;

  GameMode(String environmentName) {
    this.environmentName = environmentName;
  }

  @Override
  public String toString() {
    return environmentName;
  }
}
