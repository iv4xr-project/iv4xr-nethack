package connection;

import connection.messagedecoder.ObservationMessageDecoder;
import connection.messagedecoder.SeedDecoder;
import connection.messagedecoder.StepMessageDecoder;
import connection.messageencoder.Encoder;
import connection.messageencoder.SeedEncoder;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import nethack.object.*;
import nl.uu.cs.aplib.utils.Pair;
import util.*;

public class SocketClient {
  final String host;
  final int port;
  Socket socket;
  DataInputStream reader;
  DataOutputStream writer;

  /**
   * Constructor. Will set up the needed socket to communicate with the given server hosted at the
   * given host-id, at the given port.
   */
  public SocketClient() {
    Pair<String, Integer> info = Config.getConnectionInfo();
    this.host = info.fst;
    this.port = info.snd;
    int maxWaitTime = 60000;
    Loggers.ConnectionLogger.info(
        "Trying to connect with a host on %s:%d (will time-out after %d seconds)",
        host, port, maxWaitTime / 1000);

    long startTime = System.nanoTime();

    while (!socketReady() && millisElapsed(startTime) < maxWaitTime) {
      try {
        socket = new Socket(host, port);
        reader = new DataInputStream(socket.getInputStream());
        writer = new DataOutputStream(socket.getOutputStream());
      } catch (IOException ignored) {
      }
    }

    assert socketReady()
        : String.format("Could NOT establish a connection with the host %s:%s.", host, port);
    Loggers.ConnectionLogger.info("CONNECTED with %s:%d", host, port);
  }

  /**
   * @return true if the socket and readers are not null
   */
  public boolean socketReady() {
    return socket != null;
  }

  /**
   * @param startTimeNano the start time in long
   * @return the elapsed time from the start time converted to milliseconds
   */
  private float millisElapsed(long startTimeNano) {
    return (System.nanoTime() - startTimeNano) / 1000000f;
  }

  public void sendSetSeed(Seed seed) {
    writeBit(Encoder.EncoderBit.SetSeedBit);
    SeedEncoder.encode(writer, seed);
    flush();
  }

  public void sendReset(String gameMode) {
    writeBit(Encoder.EncoderBit.ResetBit);
    Encoder.writeString(writer, gameMode);
    flush();
  }

  public Seed sendGetSeed() {
    writeBit(Encoder.EncoderBit.GetSeedBit);
    flush();
    return readSeed();
  }

  public void sendRender() {
    writeBit(Encoder.EncoderBit.RenderBit);
    flush();
  }

  public void sendClose() {
    writeBit(Encoder.EncoderBit.CloseBit);
    flush();
  }

  public void sendSaveCoverage() {
    writeBit(Encoder.EncoderBit.SaveCoverage);
    Encoder.writeBoolean(writer, Config.getGenerateHTML());
    flush();
    Loggers.ConnectionLogger.info("Waiting on saving coverage...");
    readNullByte();
  }

  public void sendResetCoverage() {
    writeBit(Encoder.EncoderBit.ResetCoverage);
    flush();
    readNullByte();
  }

  private void readNullByte() {
    try {
      byte nullByte = reader.readByte();
      assert nullByte == 0
          : String.format("Null byte should indicate its done, value was %s", nullByte);
    } catch (IOException exception) {
      throw new RuntimeException(Arrays.toString(exception.getStackTrace()));
    }
  }

  public StepState sendStepBytes(byte[] bytes) {
    Stopwatch stopwatch = new Stopwatch(true);
    try {
      writer.write(bytes);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    flush();

    Loggers.ProfilerLogger.trace("SENDING STEP BYTES TOOK: %f", stopwatch.total());
    return readStepState();
  }

  private void writeBit(Encoder.EncoderBit bit) {
    Encoder.writeByte(writer, bit.value);
  }

  public Seed readSeed() {
    Loggers.ConnectionLogger.info("** read....");
    return SeedDecoder.decode(reader);
  }

  public ObservationMessage readObservationMessage() {
    return ObservationMessageDecoder.decode(reader);
  }

  public StepState readStepState() {
    Loggers.ConnectionLogger.info("Waiting for StepState...");
    ObservationMessage obsMessage = readObservationMessage();
    StepMessage stepMessage = StepMessageDecoder.decode(reader);
    Stopwatch stopwatch = new Stopwatch(true);
    Loggers.ProfilerLogger.trace("READ OBS MESSAGE TOOK: %f", stopwatch.split());

    StepState stepState = new StepState();
    stepState.player = obsMessage.player;
    stepState.player.location = obsMessage.player.location;
    stepState.player.inventory = new Inventory(obsMessage.items);
    stepState.stats = obsMessage.stats;
    stepState.done = stepMessage.done;
    stepState.info = null;
    stepState.level = new Level(obsMessage.entities);
    stepState.message = obsMessage.message;
    Loggers.ProfilerLogger.trace("EXIT TOOK: %f", stopwatch.split());
    return stepState;
  }

  public void flush() {
    try {
      writer.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void close() throws IOException {
    Loggers.ConnectionLogger.info("Closing socket connection");
    if (reader != null) reader.close();
    if (writer != null) writer.close();
    socket.close();
    Loggers.ConnectionLogger.info("Socket connection closed");
  }
}
