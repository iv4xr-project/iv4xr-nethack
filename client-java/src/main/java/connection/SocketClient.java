package connection;

import connection.messagedecoder.ObservationMessageDecoder;
import connection.messagedecoder.SeedDecoder;
import connection.messagedecoder.StepMessageDecoder;
import connection.messageencoder.Encoder;
import connection.messageencoder.SeedEncoder;
import eu.iv4xr.framework.spatial.Vec3;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import nethack.object.Inventory;
import nethack.object.Level;
import nethack.object.Seed;
import nethack.object.StepState;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.logging.log4j.Logger;
import util.Config;
import util.Loggers;
import util.Stopwatch;

public class SocketClient {
  static final Logger logger = Loggers.ConnectionLogger;
  static final Logger profileLogger = Loggers.ProfilerLogger;
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
    int maxWaitTime = 20000;
    logger.info(
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
    logger.info("CONNECTED with %s:%d", host, port);
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
    Encoder.sendString(writer, gameMode);
    flush();
    readObservationMessage();
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

  public StepState sendStep(int index) {
    Stopwatch stopwatch = new Stopwatch(true);
    try {
      writer.write(new byte[] {Encoder.EncoderBit.StepBit.value, (byte) index});
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    flush();
    profileLogger.trace("SENDING COMMAND TOOK: %d%n", stopwatch.total());
    return readStepState();
  }

  public StepState sendStepStroke(char stroke) {
    writeBit(Encoder.EncoderBit.StepStrokeBit);
    Encoder.sendChar(writer, stroke);
    flush();
    return readStepState();
  }

  private void writeBit(Encoder.EncoderBit bit) {
    Encoder.writeByte(writer, bit.value);
  }

  public Seed readSeed() {
    logger.info("** read....");
    return SeedDecoder.decode(reader);
  }

  public ObservationMessage readObservationMessage() {
    return ObservationMessageDecoder.decode(reader);
  }

  public StepState readStepState() {
    logger.info("Waiting for StepState...");
    ObservationMessage obsMessage = readObservationMessage();
    StepMessage stepMessage = StepMessageDecoder.decode(reader);
    Stopwatch stopwatch = new Stopwatch(true);
    profileLogger.trace("READ OBS MESSAGE TOOK: %d%n%n", stopwatch.split());

    StepState stepState = new StepState();
    stepState.player = obsMessage.player;
    stepState.player.position =
        new Vec3(obsMessage.player.position.x, obsMessage.player.position.y, 0);
    stepState.player.inventory = new Inventory(obsMessage.items);
    stepState.stats = obsMessage.stats;
    stepState.done = stepMessage.done;
    stepState.info = null;
    stepState.level = new Level(obsMessage.entities);
    stepState.message = obsMessage.message;
    profileLogger.trace("EXIT TOOK: %d%n", stopwatch.split());
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
    logger.info("Closing socket connection");
    if (reader != null) reader.close();
    if (writer != null) writer.close();
    socket.close();
  }
}
