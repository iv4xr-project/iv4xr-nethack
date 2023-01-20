package nethack.object;

import eu.iv4xr.framework.spatial.IntVec2D;
import eu.iv4xr.framework.spatial.Vec3;

public class Player {
    public static final String ID = "player";
    public Inventory inventory;
    public Vec3 previousPosition;
    public IntVec2D previousPosition2D;
    public Vec3 position;
    public IntVec2D position2D;
    public int strength;
    public int dexterity;
    public int constitution;
    public int intelligence;
    public int wisdom;
    public int charisma;
    public int hp;
    public int hpMax;
    public int gold;
    public int energy;
    public int energyMax;
    public int armorClass;
    public int experienceLevel;
    public int experiencePoints;
    public int hungerState;
    public int carryingCapacity;
    public int condition;
    public Alignment alignment;
    public Player() {
    }

    public static enum Alignment {
        LAWFUL,
        NEUTRAL,
        CHAOTIC
    }
}