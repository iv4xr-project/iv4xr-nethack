package nethack;

// Source: https://www.baeldung.com/java-enum-values
// Actions listed at: /python-server/lib/nle/nle/nethack/actions.py
public class Entity {
	public Color color;
	public char symbol;
	public EntityType type;

	Entity(char symbol, EntityType type, Color color) {
		this.color = color;
		this.symbol = symbol;
		this.type = type;
	}
	
	public static Entity fromValues(char symbol, Color color) {
		EntityType type = EntityType.fromSymbol(symbol, color);
		return new Entity(symbol, type, color);
	}
}