package connection.messageparser;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import connection.ConnectionLoggers;
import connection.ObservationMessage;
import nethack.object.*;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

// Source: https://studytrails.com/2016/09/12/java-google-json-type-adapter/
public class ObservationMessageTypeAdapter extends TypeAdapter<ObservationMessage> {
    static final Logger logger = LogManager.getLogger(ConnectionLoggers.TypeAdapterLogger);

    @Override
    public ObservationMessage read(JsonReader reader) throws IOException {
        // the first token is the start array
        ObservationMessage observationMessage = new ObservationMessage();
        JsonToken token = reader.peek();
        if (token.equals(JsonToken.BEGIN_OBJECT)) {
            reader.beginObject();
            while (reader.hasNext()) {
                String objectEntry = reader.nextName();
                switch (objectEntry) {
                    case "blstats":
                        Pair<Stats, Player> blstats = new StatsTypeAdapter().read(reader);
                        observationMessage.stats = blstats.fst;
                        observationMessage.player = blstats.snd;
                        break;
                    case "entities":
                        observationMessage.entities = readMap(reader);
                        break;
                    case "message":
                        observationMessage.message = Utils.readString(reader);
                        break;
                    case "inventory":
                        observationMessage.items = readItems(reader);
                        break;
                    default:
                        String errorMsg = String.format("Unknown observationMessage key: %s", objectEntry);
                        logger.error(errorMsg);
                        throw new IllegalArgumentException(errorMsg);
                }
            }
            reader.endObject();
            return observationMessage;
        } else {
            logger.error("Trying to read observation message, however not an object");
            return null;
        }
    }

    public Entity[][] readMap(JsonReader reader) throws IOException {
        Entity[][] row = new Entity[Level.HEIGHT][Level.WIDTH];
        JsonToken token = reader.peek();
        if (token.equals(JsonToken.BEGIN_ARRAY)) {
            reader.beginArray();
            int i = 0;
            while (!reader.peek().equals(JsonToken.END_ARRAY)) {
                row[i++] = readMapRow(reader);
            }
            reader.endArray();
        }
        return row;
    }

    public Entity[] readMapRow(JsonReader reader) throws IOException {
        EntityTypeAdapter entityTypeAdapter = new EntityTypeAdapter();
        Entity[] row = new Entity[Level.WIDTH];
        JsonToken token = reader.peek();
        if (token.equals(JsonToken.BEGIN_ARRAY)) {
            reader.beginArray();
            int i = 0;
            while (!reader.peek().equals(JsonToken.END_ARRAY)) {
                row[i++] = entityTypeAdapter.read(reader);
            }
            reader.endArray();
        }
        return row;
    }

    public Item[] readItems(JsonReader reader) throws IOException {
        ItemTypeAdapter itemTypeAdapter = new ItemTypeAdapter();
        Item[] row = new Item[Inventory.SIZE];
        JsonToken token = reader.peek();
        if (token.equals(JsonToken.BEGIN_ARRAY)) {
            reader.beginArray();
            int i = 0;
            while (!reader.peek().equals(JsonToken.END_ARRAY)) {
                row[i++] = itemTypeAdapter.read(reader);
            }
            reader.endArray();
        }
        return row;
    }

    @Override
    public void write(JsonWriter out, ObservationMessage message) throws IOException {
    }
}
