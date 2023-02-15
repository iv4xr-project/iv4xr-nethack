package util;

public class Stopwatch {
  public static boolean print = true;
  private long startTime;
  private long previousTime;
  private boolean running = false;

  public Stopwatch() {}

  public void start() {
    running = true;
    startTime = System.nanoTime();
    previousTime = startTime;
  }

  public void stop() {
    running = false;
  }

  public void printSplit(String description) {
    if (!print) {
      return;
    }

    System.out.printf("Lap time %.3fs: %s%n", split(), description);
  }

  public void printTotal(String description) {
    if (!print) {
      return;
    }

    System.out.printf("Total time %.3fs: %s%n", total(), description);
  }

  public float split() {
    assert running;
    long currentTime = time();
    float secs = toSecs(currentTime - previousTime);
    previousTime = currentTime;
    return secs;
  }

  public float total() {
    assert running : "Cannot request total time if it is not running";
    previousTime = time();
    return toSecs(previousTime - startTime);
  }

  private long time() {
    return System.nanoTime();
  }

  private float toSecs(long time) {
    return time / 1000000000.f;
  }
}
