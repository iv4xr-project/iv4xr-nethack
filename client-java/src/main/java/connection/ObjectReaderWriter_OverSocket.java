package connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import connection.messageparser.ObservationMessageTypeAdapter;
import connection.messageparser.SeedTypeAdapter;
import nethack.object.Inventory;
import nethack.object.Level;
import nethack.object.Seed;
import nethack.StepState;

/**
 * Provide a convenient reader/writer to read and write objects over a socket.
 * This allows an object to be send over the socket (to a recipient on the other
 * side of the socket-connection), encoded as a Json-string. Similarly, the
 * reader can receive an object, encoded as a Json-string, that was sent to this
 * class over the socket. Note that this implies that the object sent like this
 * must be serializable to Json.
 *
 * <p>
 * This class can be used by both a server or a client.
 *
 * <p>
 * Note: this class was taken over from iv4xrDemo.
 */
public class ObjectReaderWriter_OverSocket {
	static final Logger logger = LogManager.getLogger(ObjectReaderWriter_OverSocket.class);
	Socket socket;
	BufferedReader reader;
	PrintWriter writer;

	// Configuring the json serializer/deserializer. Register custom serializers
	// here.
	// Transient modifiers should be excluded, otherwise they will be send with json
	private static Gson gson = new GsonBuilder()
			.registerTypeAdapter(ObservationMessage.class, new ObservationMessageTypeAdapter()).serializeNulls()
			.registerTypeAdapter(Seed.class, new SeedTypeAdapter()).serializeNulls()
			.excludeFieldsWithModifiers(Modifier.TRANSIENT).create();

	public ObjectReaderWriter_OverSocket(Socket socket) throws IOException {
		this.socket = socket;
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
		writer = new PrintWriter(socket.getOutputStream(), true);
	}

	/**
	 * Send an object to the host. The object will first be serialized to a json
	 * string, so it is assumed that the json serializer knows how to handle the
	 * object.
	 * 
	 * @throws IOException
	 */
	public void write(Object packageToSend) throws IOException {
		String json = gson.toJson(packageToSend);
		logger.debug("** SENDING: " + json);
		writer.println(json);
	}

	public StepState readStepState() throws IOException {
		logger.debug("** waiting for answer....");

		reader.ready();
		String response = reader.readLine();
		ObservationMessage obsMessage = gson.fromJson(response, ObservationMessage.class);

		reader.ready();
		response = reader.readLine();
		StepMessage stepMessage = gson.fromJson(response, StepMessage.class);

		StepState stepState = new StepState();
		stepState.player = obsMessage.player;
		stepState.player.inventory = new Inventory(obsMessage.items);
		stepState.stats = obsMessage.stats;
		stepState.done = stepMessage.done;
		stepState.info = stepMessage.info;
		stepState.level = new Level(obsMessage.stats.zeroIndexLevelNumber, obsMessage.entities);
		stepState.message = obsMessage.message;
		return stepState;
	}

	/**
	 * Read an object that was sent by the host. The object will be received as a
	 * json string, which is then converted into an instance of the given class. It
	 * is assumed that the json deserializer knows how to do this. The resulting
	 * object is then returned.
	 * 
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	public <T> T read(Class<T> expectedClassOfResultObj) throws IOException {
		if (expectedClassOfResultObj == StepState.class) {
			return (T) readStepState();
		}

		logger.debug("** waiting for answer....");
		reader.ready();
		String response = reader.readLine();
		// String response = readResponse();
		// we do not have to cast to T, since req.responseType is of type Class<T>
		logger.debug("** RECEIVING: " + response);
		return gson.fromJson(response, expectedClassOfResultObj);
	}

	/**
	 * Close the reader/writer. This does NOT close the socket.
	 */
	public void close() throws IOException {
		if (reader != null)
			reader.close();
		if (writer != null)
			writer.close();
	}
}
