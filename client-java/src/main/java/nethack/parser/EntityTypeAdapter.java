package nethack.parser;

import nethack.Entity;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

// Source: https://studytrails.com/2016/09/12/java-google-json-type-adapter/
public class EntityTypeAdapter extends TypeAdapter<Entity> {
	@Override
	public Entity read(JsonReader reader) throws IOException {
		return new Entity((char)reader.nextInt());
	}

	@Override
	public void write(JsonWriter out, Entity action) throws IOException { }
}