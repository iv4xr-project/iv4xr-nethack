package connection.messageparser;

import java.io.IOException;
import java.util.OptionalLong;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import nethack.object.Seed;

public class SeedTypeAdapter extends TypeAdapter<Seed> {
	@Override
	public Seed read(JsonReader reader) throws IOException {
		String core = "";
		String disp = "";
		Boolean reseed = false;

		JsonToken token = reader.peek();
		if (token.equals(JsonToken.BEGIN_OBJECT)) {
			reader.beginObject();
			while (reader.hasNext()) {
				String objectEntry = reader.nextName();
				switch (objectEntry) {
				case "core":
					core = reader.nextString();
					break;
				case "disp":
					disp = reader.nextString();
					break;
				case "reseed":
					reseed = reader.nextBoolean();
					break;
				default:
					System.out.println(objectEntry + " has not been recognize when parsing observationMessage.");
					break;
				}
			}
			reader.endObject();
			return new Seed(core, disp, reseed);
		} else {
			System.out.println("Not an object");
			return null;
		}
	}

	@Override
	public void write(JsonWriter out, Seed seed) throws IOException {
		out.beginObject();
		
		if (seed.core.isPresent()) {
			out.name("core");
			out.value(seed.coreSeed);
		}
		if (seed.disp.isPresent()) {
			out.name("disp");
			out.value(seed.dispSeed);
		}
		out.name("reseed");
		out.value(seed.reseed);
		out.endObject();
	}
}
