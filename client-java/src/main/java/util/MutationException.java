package util;

public class MutationException extends Exception {
  public MutationException() {
    super();
  }

  public MutationException(String message) {
    super(message);
  }

  public MutationException(String message, Throwable cause) {
    super(message, cause);
  }

  public MutationException(Throwable cause) {
    super(cause);
  }
}
