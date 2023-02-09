//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar;

import eu.iv4xr.framework.spatial.IntVec2D;

public class Size {
  public int height;
  public int width;

  public Size(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public void withinBounds(IntVec2D pos) {
    assert pos.x >= 0 && pos.x < width && pos.y >= 0 && pos.y < height
        : String.format("Position outside bounds %s (size=%s)", pos, toString());
  }

  public String toString() {
    return String.format("(w:%d,h:%d)", width, height);
  }
}
