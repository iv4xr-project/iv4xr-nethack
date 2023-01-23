package connection;

import java.io.IOException;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A class that supports sending a pair (cmd,arg) to a server and receives the response the server
 * sends. A socket is used to facilitate the connection to the server. From the perspective of a
 * client-server relation, this class acts as a client.
 *
 * <p>The pair (cmd,arg) represents some command and its argument. When sent to the server, the
 * server can interpret the command and produce some result, which is then sent back to the client
 * (this class) as a response.
 *
 * <p>This class provides a method that allows us to abstractly see the sending such a command as a
 * function call r = cmd(arg), where r is the the server's result/response on that command.
 *
 * <p>The command (cmd,arg) is sent over as a Json string. And the server is assumed to send back
 * each response object as a Json string.
 *
 * @author Wish
 */
public class SendCommandClient {
  static final Logger logger = LogManager.getLogger(ConnectionLoggers.ConnectionLogger);
  String host;
  int port;
  Socket socket;
  ObjectReaderWriter_OverSocket readerwriter;

  /**
   * Constructor. Will setup the needed socket to communicate with the given server hosted at the
   * given host-id, at the given port.
   */
  public SendCommandClient(String host, int port) {
    this.host = host;
    this.port = port;
    int maxWaitTime = 20000;
    logger.info(
        String.format(
            "> Trying to connect with a host on %s:%s (will time-out after %s seconds)",
            host, port, maxWaitTime / 1000));

    long startTime = System.nanoTime();

    while (!socketReady() && millisElapsed(startTime) < maxWaitTime) {
      try {
        socket = new Socket(host, port);
        readerwriter = new ObjectReaderWriter_OverSocket(socket);
      } catch (IOException ignored) {
      }
    }
    if (socketReady()) {
      logger.info(String.format("> CONNECTED with %s:%s", host, port));
    } else {
      logger.warn(
          String.format("> Could NOT establish a connection with the host %s:%s.", host, port));
    }
  }

  /**
   * @return true if the socket and readers are not null
   */
  public boolean socketReady() {
    return socket != null && readerwriter != null;
  }

  /**
   * @param startTimeNano the start time in long
   * @return the elapsed time from the start time converted to milliseconds
   */
  private float millisElapsed(long startTimeNano) {
    return (System.nanoTime() - startTimeNano) / 1000000f;
  }

  /**
   * Send a command and an argument to the server. The pair will first be wrapped as an instance of
   * the class Cmd, and then sent to the server as a Json-string. The server is expected to
   * interpret the command, does some calculation, and produces some result. This will be sent back
   * to the client (this class) as a Json-string, which this method will return as an object. The
   * Json-string will be converted to an object of some class T, as specified in the 3rd parameter
   * of this method.
   */
  public <T> T sendCommand(String cmd, Object arg, Class<T> expectedClassOfResultObj) {
    try {
      readerwriter.write(new Cmd(cmd, arg));
      return readerwriter.read(expectedClassOfResultObj);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

  // Send a command without a return type
  public void writeCommand(String cmd, Object arg) {
    try {
      readerwriter.write(new Cmd(cmd, arg));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public <T> T read(Class<T> expectedClassOfResultObj) {
    try {
      return readerwriter.read(expectedClassOfResultObj);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

  public void close() throws IOException {
    readerwriter.close();
    socket.close();
  }

  static class Cmd {
    String cmd;
    Object arg;

    Cmd(String cmd, Object arg) {
      this.cmd = cmd;
      this.arg = arg;
    }
  }
}
