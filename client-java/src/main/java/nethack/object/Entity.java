package nethack.object;

// Source: https://www.baeldung.com/java-enum-values
// Actions listed at: /python-server/lib/nle/nle/nethack/actions.py
public class Entity {
	public Color color;
	public char symbol;
	public EntityType type;
	public String id;

	Entity(char symbol, EntityType type, Color color) {
		this.color = color;
		this.symbol = symbol;
		this.type = type;
	}
	
	public boolean closedDoor() {
		if (type != EntityType.DOOR) {
			System.out.println("NOT A DOOR?!");
		}
		
		return symbol == '+';
	}

	public void assignId(int x, int y) {
		id = String.format("%s_%d_%d", type.name(), x, y);
	}

	public static Entity fromValues(char symbol, Color color) {
		EntityType type = EntityType.fromSymbol(symbol, color);
		return new Entity(symbol, type, color);
	}

	public boolean becameTransparent(Entity newState) {
		if (!newState.color.equals(Color.TRANSPARENT) || color.equals(Color.TRANSPARENT)) {
			return false;
		}
		return newState.symbol == symbol && newState.type == type;
	}

	public boolean becameVisible(Entity newState) {
		if (!color.equals(Color.TRANSPARENT) || newState.color.equals(Color.TRANSPARENT)) {
			return false;
		}
		return newState.symbol == symbol && newState.type == type;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (!Entity.class.isAssignableFrom(obj.getClass())) {
			return false;
		}

		Entity other = (Entity) obj;
		return other.color.equals(color) && other.symbol == symbol && other.type == type;
	}
}