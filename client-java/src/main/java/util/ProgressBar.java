package util;

// Source: https://masterex.github.io/archive/2011/10/23/java-cli-progress-bar.html

import nethack.object.Turn;

/**
 * Ascii progress meter. On completion this will reset itself, so it can be reused <br>
 * <br>
 * 100% ################################################## |
 */
public class ProgressBar {
  private StringBuilder progress;

  /** initialize progress bar properties. */
  public ProgressBar() {
    init();
  }

  /**
   * called whenever the progress bar needs to be updated. that is whenever progress was made.
   *
   * @param turn a Turn representing the work done so far
   * @param desiredTurn a Turn representing the total work
   */
  public void updateTurn(Turn turn, Turn desiredTurn) {
    char[] workChars = {'|', '/', '-', '\\'};

    String format = "\r%3d(%d)/%d(%d) %s %c ";

    int percent = (turn.time * 100) / desiredTurn.time;
    int extraChars = (percent / 2) - progress.length();

    progress.append("#".repeat(extraChars));
    System.out.printf(
        format,
        turn.time,
        turn.step,
        desiredTurn.time,
        desiredTurn.step,
        progress,
        workChars[turn.time % workChars.length]);

    if (turn.compareTo(desiredTurn) >= 0) {
      System.out.flush();
      init();
    }
  }

  private void init() {
    progress = new StringBuilder(60);
  }
}
