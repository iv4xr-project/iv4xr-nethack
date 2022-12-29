package nethack;

import java.util.HashMap;
import java.util.Map;

public enum EntityType {
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
	
	static EntityType fromSymbol(char symbol, Color color) {
		// When simply the character is enough to identify the type
		if (BY_SYMBOL.containsKey(symbol) ) {
			return BY_SYMBOL.get(symbol);
		}
		
		switch(symbol) {
			case '|':
			case '-':
				return color == Color.BROWN ? EntityType.DOOR : EntityType.WALL;
			case '+':
				return color == Color.BROWN ? EntityType.DOOR : EntityType.SPELLBOOK;
			case 'd':
			case 'f':
			case 'u':
				return color == Color.WHITE ? EntityType.PET : EntityType.MONSTER;
			case '@':
				return color == Color.WHITE ? EntityType.PLAYER : EntityType.HUMAN;
		}
		
		return EntityType.UNKNOWN;
	}
}

