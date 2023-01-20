package agent.navigation.surface;

import agent.navigation.NethackSurface;
import eu.iv4xr.framework.spatial.IntVec2D;

public class Prisonbars extends Obstacle {
    public final boolean seeThrough = true;
    public Prisonbars(IntVec2D pos) { super(pos); }

    @Override
    public char toChar() { return seen ? 'P' : 'p'; }
}