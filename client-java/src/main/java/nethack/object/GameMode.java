package nethack.object;

public enum GameMode {
	NethackChallenge("NetHackChallenge-v0"), // Does not allow seeding
	Nethack("NetHack-v0"), // Allows for seeding
	;
	
	private String environmentName;
	
	GameMode(String environmentName) {
		this.environmentName = environmentName;
	}
	
	@Override
	public String toString() {
		return environmentName;
	}
}