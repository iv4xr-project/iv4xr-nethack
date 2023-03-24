package nethack.world;

import agent.navigation.GridSurface;

public class Surface extends GridSurface {
  public Surface() {
    super(Level.SIZE, 8);

    // hierarchicalNav.hierarchicalGraph.createEdgesWithinLevel(levelNr, surface);
  }
}
