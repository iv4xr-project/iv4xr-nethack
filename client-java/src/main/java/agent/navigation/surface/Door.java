package agent.navigation.surface;

import eu.iv4xr.framework.spatial.IntVec2D;

public class Door extends Obstacle {
    public boolean isOpen = false;
    public boolean seeThrough = isOpen;

    public Door(IntVec2D pos) { super(pos); }

    public Door(IntVec2D pos, boolean isOpen) {
        super(pos);
        this.isOpen = isOpen;
    }

    @Override
    public char toChar() {
        if (isOpen) {
            return seen ? 'O' : 'o';
        } else {
            return seen ? 'X' : 'x';
        }
    }
}