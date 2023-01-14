package connection.messageparser;

import java.io.IOException;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public class Utils {
	public static String readString(JsonReader reader) throws IOException {
		String message = "";
		JsonToken token = reader.peek();
		if (token.equals(JsonToken.BEGIN_ARRAY)) {
			reader.beginArray();
			while (!reader.peek().equals(JsonToken.END_ARRAY)) {
				message += (char) reader.nextInt();
			}
			reader.endArray();
			return message;
		} else {
			return null;
		}
	}
}
