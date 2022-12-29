package nethack.parser;

import nethack.Color;
import nethack.Entity;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

// Source: https://studytrails.com/2016/09/12/java-google-json-type-adapter/
public class EntityTypeAdapter extends TypeAdapter<Entity> {
	@Override
	public Entity read(JsonReader reader) throws IOException {
		// the first token is the start array
		JsonToken token = reader.peek();
		if (token.equals(JsonToken.BEGIN_ARRAY)) {
			reader.beginArray();
			char symbol = (char)reader.nextInt();
			Color color = new ColorTypeAdapter().read(reader);
			reader.endArray();
			return Entity.fromValues(symbol, color);
		} else {
			return null;
		}
	}

	@Override
	public void write(JsonWriter out, Entity action) throws IOException { }
}