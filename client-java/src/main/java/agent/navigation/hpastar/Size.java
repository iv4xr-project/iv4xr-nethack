//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar;

import util.CustomVec2D;

public class Size {
  public final int height;
  public final int width;

  public Size(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public void withinBounds(CustomVec2D pos) {
    assert pos.x >= 0 && pos.x < width && pos.y >= 0 && pos.y < height
        : String.format("Position outside bounds %s (size=%s)", pos, this);
  }

  public String toString() {
    return String.format("(w:%d,h:%d)", width, height);
  }
}
