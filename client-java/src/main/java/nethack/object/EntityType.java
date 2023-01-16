package nethack.object;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum EntityType implements Serializable {
    ALTAR('_'),
    AMULET('"'),
    ARMOR('['),
    BALL('0'),
    BOULDER('`'),
    CORRIDOR('#'),
    DOOR('▉'),
    SPELLBOOK('▉'),
    EDIBLE('%'),
    FLOOR('.'),
    FOUNTAIN('{'),
    GEM('▉'),
    GRAVE('▉'),
    GOLD('$'),
    HUMAN('▉'),
    ITEM('('),
    LAST_LOCATION('I'),
    MONSTER('▉'),
    PLAYER('▉'),
    PET('▉'),
    POOL('}'),
    POTION('!'),
    RING('='),
    ROCK('▉'),
    SCROLL('?'),
    STAIRS_DOWN('>'),
    STAIRS_UP('<'),
    SPIDER_WEB('▉'),
    STATUE('▉'),
    THRONE('\\'),
    TRAP('^'),
    UNKNOWN('▉'),
    VOID(' '),
    WALL('▉'),
    WAND('/'),
    WEAPON(')');

    private static final Map<Character, EntityType> BY_SYMBOL = new HashMap<>();
    private static final long serialVersionUID = 1L;

    static {
        for (EntityType e : values()) {
            BY_SYMBOL.put(e.symbol, e);
        }
    }

    private char symbol;

    EntityType(char symbol) {
        this.symbol = symbol;
    }

    static EntityType fromSymbol(char symbol, Color color) {
        // When simply the character is enough to identify the type
        if (BY_SYMBOL.containsKey(symbol)) {
            return BY_SYMBOL.get(symbol);
        }

        switch (symbol) {
            case '|':
                if (color == Color.BROWN)
                    return EntityType.DOOR;
                if (color == Color.WHITE)
                    return EntityType.GRAVE;
                return EntityType.WALL;
            case '-':
                if (color == Color.BROWN)
                    return EntityType.DOOR;
                return EntityType.WALL;
            case '+':
                return color == Color.BROWN ? EntityType.DOOR : EntityType.SPELLBOOK;
            case 'd':
            case 'f':
                return color == Color.WHITE ? EntityType.PET : EntityType.MONSTER;
            case 'u':
                return color == Color.BROWN ? EntityType.PET : EntityType.MONSTER;
            case '@':
                return color == Color.WHITE ? EntityType.PLAYER : EntityType.HUMAN;
        }

        if (Character.isAlphabetic(symbol) || symbol == ':') {
            if (color == Color.WHITE) {
                return EntityType.STATUE;
            }
            return EntityType.MONSTER;
        }

        return EntityType.UNKNOWN;
    }
}
