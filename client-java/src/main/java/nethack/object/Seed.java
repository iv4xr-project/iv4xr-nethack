package nethack.object;

import java.util.OptionalInt;

// From Python documentation:
// Sets the state of the NetHack RNGs after the next reset.
// NetHack 3.6 uses two RNGs, core and disp. This is to prevent RNG-manipulation by e.g. running into walls or other no-ops 
// on the actual game state. This is a measure against "tool-assisted speedruns" (TAS). 
// NLE can run in both NetHack's default mode and in TAS-friendly "no reseeding" if reseed is set to False

// As an Anti-TAS (automation) measure,
// NetHack 3.6 reseeds with true randomness every now and then. This
// flag enables or disables this behavior. If set to True, trajectories
// won't be reproducible.

public class Seed {
	public OptionalInt core;
	public OptionalInt disp;
	public boolean reseed;
	
	public Seed(OptionalInt core, OptionalInt disp, boolean reseed) {
		this.core = core;
		this.disp = disp;
		this.reseed = reseed;
	}
	
	public static Seed simple() {
		return new Seed(OptionalInt.of(0), OptionalInt.of(0), false);
	}
	
	@Override
	public String toString() {
		String coreStr = core.isPresent() ? "core:" + core.getAsInt() : "None";
		String dispStr = disp.isPresent() ? "disp:" + disp.getAsInt() : "None";
		return String.format("%s %s reseed:%b", coreStr, dispStr, reseed);
	}
}
