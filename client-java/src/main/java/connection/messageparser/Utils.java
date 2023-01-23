package connection.messageparser;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.IOException;

public class Utils {
  public static String readString(JsonReader reader) throws IOException {
    StringBuilder message = new StringBuilder();
    JsonToken token = reader.peek();
    if (token.equals(JsonToken.BEGIN_ARRAY)) {
      reader.beginArray();
      while (!reader.peek().equals(JsonToken.END_ARRAY)) {
        int charCode = reader.nextInt();
        if (charCode == 0) {
          continue;
        }
        message.append((char) charCode);
      }
      reader.endArray();
      return message.toString();
    } else {
      return null;
    }
  }
}
