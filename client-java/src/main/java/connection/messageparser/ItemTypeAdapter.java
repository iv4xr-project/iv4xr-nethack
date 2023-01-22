package connection.messageparser;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import connection.ConnectionLoggers;
import nethack.object.Item;
import nethack.enums.ItemType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;


public class ItemTypeAdapter extends TypeAdapter<Item> {
    static final Logger logger = LogManager.getLogger(ConnectionLoggers.TypeAdapterLogger);
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
            logger.warn("Tried to read an item, however it was not in an array");
            return null;
        }
    }

    @Override
    public void write(JsonWriter out, Item item) throws IOException {
    }
}