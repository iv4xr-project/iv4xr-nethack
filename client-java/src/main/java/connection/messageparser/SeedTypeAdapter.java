package connection.messageparser;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import connection.ConnectionLoggers;
import java.io.IOException;
import nethack.object.Seed;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SeedTypeAdapter extends TypeAdapter<Seed> {
  static final Logger logger = LogManager.getLogger(ConnectionLoggers.TypeAdapterLogger);

  @Override
  public Seed read(JsonReader reader) throws IOException {
    String core = "";
    String disp = "";
    boolean reseed = false;

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
            logger.error(String.format("Seed field \"%s\" unknown", objectEntry));
            throw new IllegalArgumentException(
                String.format("Seed field \"%s\" unknown", objectEntry));
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
