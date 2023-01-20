package agent.navigation.surface;

import eu.iv4xr.framework.spatial.IntVec2D;

public class Doorway extends Tile {
    public final boolean seeThrough = true;
    public Doorway(IntVec2D pos) { super(pos); }

    @Override
    public char toChar() { return '·'; }
}
