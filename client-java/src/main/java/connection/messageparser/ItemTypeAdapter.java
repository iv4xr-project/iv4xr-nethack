package connection.messageparser;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import nethack.object.Item;
import nethack.object.ItemType;

import java.io.IOException;


public class ItemTypeAdapter extends TypeAdapter<Item> {
    @Override
    public Item read(JsonReader reader) throws IOException {
        // the first token is the start array
        JsonToken token = reader.peek();
        if (token.equals(JsonToken.BEGIN_ARRAY)) {
            reader.beginArray();
            char symbol = (char) reader.nextInt();
            ItemType type = ItemType.values()[reader.nextInt()];
            String description = Utils.readString(reader).trim();

            reader.endArray();
            if (type == ItemType.NONE) {
                return null;
            }
            return new Item(symbol, type, description);
        } else {
            return null;
        }
    }

    @Override
    public void write(JsonWriter out, Item item) throws IOException {
    }
}