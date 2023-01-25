package nethack.object;

import java.util.Random;

// From Python documentation:
// Sets the state of the NetHack RNGs after the next reset.
// NetHack 3.6 uses two RNGs, core and disp. This is to prevent RNG-manipulation by e.g. running
// into walls or other no-ops
// on the actual game state. This is a measure against "tool-assisted speed-runs" (TAS).
// NLE can run in both NetHack's default mode and in TAS-friendly "no reseeding" if reseed is set to
// False

// As an Anti-TAS (automation) measure,
// NetHack 3.6 reseeds with true randomness sporadically. This
// flag enables or disables this behavior. If set to True, trajectories
// won't be reproducible.

public class Seed {
  public static final Seed[] presets =
      new Seed[] {
        new Seed("15175518238868522894", "13159468426723296085", false), // Room with closed door
        new Seed("3109400832504553971", "9958254807296285881", false), // Start room only walls
        new Seed("1978032860031487687", "16734812270076554578", false), // Shop at level 4
        new Seed("0", "0", false), // Portal left of player
      };
  public boolean reseed;
  public String disp;
  public String core;

  public Seed(String core, String disp, boolean reseed) {
    this.reseed = reseed;
    this.disp = disp;
    this.core = core;
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
    return String.format(
        "%s %s %b [return new Seed(\"%s\", \"%s\", %b);]", core, disp, reseed, core, disp, reseed);
  }
}
