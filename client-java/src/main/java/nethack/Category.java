//package nethack;
//
//import java.util.HashMap;
//import java.util.Map;
//
//// Source: https://www.baeldung.com/java-enum-values
//// Actions listed at: /python-server/lib/nle/nle/nethack/actions.py
//public enum Category {
//	VOID(' '),
//	HOR_WALL('-'),
//	VER_WALL('|'),
//	FLOOR('.'),
//	CORRIDOR('#'),
//	STAIRS_DOWN('>'),
//	STAIRS_UP('<'),
//	DOOR('+'),
//	HUMAN('@'),
//	GOLD('$'),
//	TRAP('^'),
//	WEAPON(')'),
//	ARMOR('['),
//	EDIBLE('%'),
//	SCROLL('?'),
//	WAND('/'),
//	RING('='),
//	POTION('!'),
//	ITEM('('),
//	ROCK('*'),
//	BOULDER('`'),
//	BALL('0'),
//	ALTAR('_'),
//	FOUNTAIN('{'),
//	POOL('}'),
//	THRONE('\\'),
//	LAST_LOCATION('I'),
//	CREATURE('▉');
//	
//	public char symbol;
//	private static final Map<Character, Category> BY_SYMBOL = new HashMap<>();
//	
//	static {
//        for (Category e: values()) {
//        	BY_SYMBOL.put(e.symbol, e);
//        }
//    }
//	
//	Category(char symbol) {
//		this.symbol = symbol;
//	}
//	
//	public static Category fromSymbol(char symbol) {
//		if (BY_SYMBOL.containsKey(symbol) ) {
//			return BY_SYMBOL.get(symbol);
//		}
//		Category creature = Category.CREATURE;
//		creature.symbol = symbol;
//		return creature;
//    }
//}