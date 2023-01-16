package nethack.object;

public class Item {
    public char symbol;
    public ItemType type;
    public String description;

    public Item(char symbol, ItemType type, String description) {
        this.symbol = symbol;
        this.type = type;
        this.description = description;
    }

    @Override
    public String toString() {
        String formatStr = "%s %-" + ItemType.maxLength() + "s %s";
        return String.format(formatStr, symbol, type, description);
    }
}
