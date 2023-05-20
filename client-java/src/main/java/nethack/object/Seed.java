package nethack.object;

import java.io.Serializable;
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

public class Seed implements Serializable {
  public final String core;

  public Seed(String core) {
    this.core = core;
  }

  // Generate a random seed
  public static Seed randomSeed() {
    Random random = new Random();
    String coreSeed = Long.toUnsignedString(random.nextLong());
    return new Seed(coreSeed);
  }

  public String shortString() {
    return String.format("%s", core);
  }

  @Override
  public String toString() {
    return String.format("[SEED=%s]", core);
  }
}
