package connection.messageparser;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import nethack.object.Color;
import nethack.object.Entity;
import nethack.object.EntityType;

import java.io.IOException;

// Source: https://studytrails.com/2016/09/12/java-google-json-type-adapter/
public class EntityTypeAdapter extends TypeAdapter<Entity> {
    @Override
    public Entity read(JsonReader reader) throws IOException {
        // the first token is the start array
        JsonToken token = reader.peek();
        if (token.equals(JsonToken.BEGIN_ARRAY)) {
            reader.beginArray();
            int glyph = (int) reader.nextInt();
            char symbol = (char) reader.nextInt();
            Color color = new ColorTypeAdapter().read(reader);
            reader.endArray();

            // Infer Entity type
            EntityType type = Utils.toEntityType(glyph, symbol, color);
            return new Entity(glyph, symbol, type, color);
        } else {
            return null;
        }
    }

    @Override
    public void write(JsonWriter out, Entity entity) throws IOException {
    }
}