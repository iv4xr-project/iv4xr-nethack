package agent.navigation.surface;

import eu.iv4xr.framework.spatial.IntVec2D;

public class Wall extends Obstacle {
    public final boolean seeThrough = false;
    public Wall(IntVec2D pos) { super(pos); }

    @Override
    public char toChar() { return seen ? 'W' : 'w'; }
}