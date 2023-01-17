package connection.messageparser;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import nethack.object.Color;
import nethack.object.EntityType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Utils {
    public static String readString(JsonReader reader) throws IOException {
        StringBuilder message = new StringBuilder();
        JsonToken token = reader.peek();
        if (token.equals(JsonToken.BEGIN_ARRAY)) {
            reader.beginArray();
            while (!reader.peek().equals(JsonToken.END_ARRAY)) {
                int charCode = reader.nextInt();
                if (charCode == 0) {
                    continue;
                }
                message.append((char) charCode);
            }
            reader.endArray();
            return message.toString();
        } else {
            return null;
        }
    }

    public static EntityType toEntityType(int glyph, char symbol, Color color) {
        EntityType type = toEntityType(glyph);
        if (type != EntityType.UNKNOWN) {
            return type;
        }

        type = toEntityType(symbol, color);
        if (type != EntityType.UNKNOWN) {
            return type;
        }

        System.out.println(String.format("%s%s%s: %d UNKNOWN", color.stringCode(), symbol, Color.RESET.stringCode(), glyph));
        return EntityType.UNKNOWN;
    }

    private static EntityType toEntityType(int glyph) {
        switch (glyph) {
            case 2379:
            case 2378:
                return EntityType.FLOOR;
            case 2371:
                return EntityType.DOORWAY;
        }

        return EntityType.UNKNOWN;
    }

    private static EntityType toEntityType(char symbol, Color color) {
        // When simply the symbol and color is enough to identify the type
        switch (symbol) {
            case '_': return EntityType.ALTAR;
            case '"': return EntityType.AMULET;
            case '[': return EntityType.ARMOR;
            case '0': return EntityType.BALL;
            case '`': return EntityType.BOULDER;
            case '#': return EntityType.CORRIDOR;
            case '+':
                return color == Color.BROWN ? EntityType.DOOR : EntityType.SPELLBOOK;
                // DOORWAY;
            case '%': return EntityType.EDIBLE;
            case '.':
                return color == Color.BLUE_BRIGHT ? EntityType.ICE : EntityType.FLOOR;
            case '{': return EntityType.FOUNTAIN;
            // GEM/ROCK
            case '|':
            case '-':
                if (color == Color.BROWN)
                    return EntityType.DOOR;
                if (color == Color.WHITE)
                    return EntityType.GRAVE;
                return EntityType.WALL;
            case '$': return EntityType.GOLD;
            case '@':
                return color == Color.WHITE ? EntityType.PLAYER : EntityType.HUMAN;
            case '(': return EntityType.ITEM;
            case 'I':
                return color == Color.TRANSPARENT ? EntityType.LAST_LOCATION : EntityType.MONSTER;
            case 'd':
            case 'f':
                return color == Color.WHITE ? EntityType.PET : EntityType.MONSTER;
            case 'u':
                return color == Color.BROWN ? EntityType.PET : EntityType.MONSTER;
            case '}': return EntityType.POOL;
            case '!': return EntityType.POTION;
            case '=': return EntityType.RING;
            case '?': return EntityType.SCROLL;
            case '>': return EntityType.STAIRS_DOWN;
            case '<': return EntityType.STAIRS_UP;
            // SPIDERWEB
            case '\\': return EntityType.THRONE;
            case '^':
                return color == Color.MAGENTA ? EntityType.PORTAL : EntityType.TRAP;
            case ' ': return EntityType.VOID;
            case '/': return EntityType.WAND;
            case ')': return EntityType.WEAPON;
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
