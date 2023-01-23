package connection.messageparser;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import nethack.enums.Color;

// Source: https://studytrails.com/2016/09/12/java-google-json-type-adapter/
public class ColorTypeAdapter extends TypeAdapter<Color> {
  @Override
  public Color read(JsonReader reader) throws IOException {
    return Color.fromValue(reader.nextInt());
  }

  @Override
  public void write(JsonWriter out, Color action) throws IOException {}
}
