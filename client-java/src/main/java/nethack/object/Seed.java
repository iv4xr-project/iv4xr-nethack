package nethack.object;

import java.util.OptionalLong;
import java.util.Random;

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
	public OptionalLong core;
	public OptionalLong disp;
	public boolean reseed;
	
	public String dispSeed;
	public String coreSeed;

	public static final Seed[] presets = new Seed[] {
		new Seed("15175518238868522894", "13159468426723296085", false), // Enclosed room
		new Seed("0", "0", false), // Portal left of player
	};
	
	public Seed(String core, String disp, boolean reseed) {
		this.core = OptionalLong.of(Long.parseUnsignedLong(core));
		this.disp = OptionalLong.of(Long.parseUnsignedLong(disp));
		this.reseed = reseed;
		
		this.dispSeed = disp;
		this.coreSeed = core;
	}
	
	// Generate a random seed
	public static Seed randomSeed() {
		Random random = new Random();
		String coreSeed = Long.toUnsignedString(random.nextLong());
		String dspSeed = Long.toUnsignedString(random.nextLong());
		
		return new Seed(coreSeed, dspSeed, false);
	}
	
	@Override
	public String toString() {	
		String coreStr = core.isPresent() ? coreSeed : "";
		String dispStr = disp.isPresent() ? dispSeed : "";
		return String.format("%s %s %b [return new Seed(\"%s\", \"%s\", %b);]", coreSeed, dispSeed, reseed, coreStr, dispStr, reseed);
	}
}
