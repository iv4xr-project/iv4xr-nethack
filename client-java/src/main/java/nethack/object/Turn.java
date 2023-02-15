package nethack.object;

import org.jetbrains.annotations.NotNull;

public class Turn implements Comparable<Turn> {
  public int time = 1;
  public int step = 0;
  public int turnNr = 0;

  public Turn(int time) {
    assert time > 0 : "Turn with 0 or negative time does not exist";
    this.time = time;
  }

  public static Turn startTurn = new Turn(1);

  public void updateTurn(Turn previousTurn) {
    if (previousTurn.time == time) {
      step = previousTurn.step + 1;
    }
    this.turnNr = previousTurn.turnNr + 1;
    assert step >= 0 : "Step must be 0 or higher";
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Turn)) {
      return false;
    }
    Turn other = (Turn) object;
    return turnNr == other.turnNr && time == other.time && step == other.step;
  }

  @Override
  public int compareTo(@NotNull Turn o) {
    if (equals(o)) {
      return 0;
    }

    if (time != o.time) {
      return Integer.compare(time, o.time);
    }

    return Integer.compare(step, o.step);
  }
}
