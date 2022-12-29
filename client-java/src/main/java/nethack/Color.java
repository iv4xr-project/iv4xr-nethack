package nethack;

import java.util.HashMap;
import java.util.Map;

public enum Color {
	// Reference file: server-python/lib/nle/src/decl.c
	// Stack-overflow on terminal colors: https://www.w3schools.blog/ansi-colors-java
	// Prefixes: https://invisible-island.net/xterm/ctlseqs/ctlseqs.html (Under: Character Attributes (SGR))
	// Complete documentation: https://www.ecma-international.org/wp-content/uploads/ECMA-48_5th_edition_june_1991.pdf (8.3.117 SGR - Select graphic rendition)
	// Example color PNG: https://github.com/remkop/picocli/commit/086bafdef1de14b320b03b34b5e5820597dd98aa
	BLACK(0, "0;30"),
	RED(1, "0;91"),
	GREEN(2, "0;32"),
	BROWN(3, "38;5;173"),
	BLUE(4, "0;34"),
	MAGENTA(5, "38;5;171"),
	CYAN(6, "38;5;51"),
	GRAY(7, "0;90"),
	TRANSPARENT(8, "38;5;239"),
	ORANGE(9, "38;5;208"),
	GREEN_BRIGHT(10, "0;92"),
	YELLOW(11, "0;93"),
	BLUE_BRIGHT(12, "38;5;33"),
	MAGENTA_BRIGHT(13, "38;5;207"),
	CYAN_BRIGHT(14, "0;96"),
	WHITE(15, "0;97");
	
	private int value;
	public String colorCode;
	private static final Map<Integer, Color> BY_VALUE = new HashMap<>();
	
	static {
        for (Color e: values()) {
            BY_VALUE.put(e.value, e);
        }
    }
	
	Color(int value, String colorCode) {
		this.value = value;
		this.colorCode = colorCode;
	}
	
	public static Color fromValue(int value) {
		if (BY_VALUE.containsKey(value) ) {
			return BY_VALUE.get(value);
		}
		throw new IllegalArgumentException("Color value not known: " + value);
    }
}