package nethack;

import java.util.HashMap;
import java.util.Map;

public enum EntityType {
	ALTAR('_'),
	AMULET('▉'),
	ARMOR('['),
	BALL('0'),
	BOULDER('`'),
	CORRIDOR('#'),
	CREATURE('▉'),
	DOOR('▉'),
	SPELLBOOK('▉'),
	EDIBLE('%'),
	FLOOR('.'),
	FOUNTAIN('{'),
	GOLD('$'),
	HUMAN('@'),
	ITEM('('),
	LAST_LOCATION('I'),
	PET('▉'),
	POOL('}'),
	POTION('!'),
	RING('='),
	ROCK('*'),
	SCROLL('?'),
	STAIRS_DOWN('>'),
	STAIRS_UP('<'),
	SPIDER_WEB('▉'),
	THRONE('\\'),
	TRAP('^'),
	UNKNOWN('▉'),
	VOID(' '),
	WALL('▉'),
	WAND('/'),
	WEAPON(')');
	
	private char symbol;
	private static final Map<Character, EntityType> BY_SYMBOL = new HashMap<>();
	
	static {
        for (EntityType e: values()) {
        	BY_SYMBOL.put(e.symbol, e);
        }
    }
	
	EntityType(char symbol) {
		this.symbol = symbol;
	}
	
	static EntityType fromSymbol(char symbol) {
		if (BY_SYMBOL.containsKey(symbol) ) {
			return BY_SYMBOL.get(symbol);
		}
		return EntityType.UNKNOWN;
	}
}

