package nethack.utils;

import nethack.Blstats;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

// Source: https://studytrails.com/2016/09/12/java-google-json-type-adapter/
public class BlstatsTypeAdapter extends TypeAdapter<Blstats> {
	@Override
	public Blstats read(JsonReader reader) throws IOException {
		// the first token is the start array
		int[] values = new int[27];
		JsonToken token = reader.peek();
		if (token.equals(JsonToken.BEGIN_ARRAY)) {
			reader.beginArray();
			int i = 0;
			while (!reader.peek().equals(JsonToken.END_ARRAY)) {
				values[i++] = reader.nextInt();
				
			}
			reader.endArray();
		}
		return new Blstats(values);
	}
	
	@Override
	public void write(JsonWriter out, Blstats action) throws IOException {

	}
}