package nethack;

// Source: https://www.baeldung.com/java-enum-values
// Actions listed at: /python-server/lib/nle/nle/nethack/actions.py
public class Entity {
//	public int color;
	public EntityType entityType;
	public char symbol;

	public Entity(char symbol) {
		this.symbol = symbol;
		this.entityType = EntityType.fromSymbol(symbol);
	}
}