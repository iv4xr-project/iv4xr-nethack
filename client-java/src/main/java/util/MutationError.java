package util;

public class MutationError extends Error {
  public MutationError() {
    super();
  }

  public MutationError(String message) {
    super(message);
  }

  public MutationError(String message, Throwable cause) {
    super(message, cause);
  }

  public MutationError(Throwable cause) {
    super(cause);
  }
}
