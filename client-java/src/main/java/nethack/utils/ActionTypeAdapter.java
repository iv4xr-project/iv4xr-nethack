package nethack.utils;

import nethack.Action;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

// Source: https://studytrails.com/2016/09/12/java-google-json-type-adapter/
public class ActionTypeAdapter extends TypeAdapter<Action> {
	@Override
	public Action read(JsonReader reader) throws IOException {
		return Action.fromValue(reader.nextInt());
	}
	
	@Override
	public void write(JsonWriter out, Action action) throws IOException {
		out.value(action.value);
	}
}