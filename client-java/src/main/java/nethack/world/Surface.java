package nethack.world;

import agent.navigation.GridSurface;
import agent.navigation.surface.Tile;
import java.util.HashSet;
import java.util.Set;
import nethack.enums.Color;
import nethack.world.tiles.Secret;
import nethack.world.tiles.Shop;
import nethack.world.tiles.Viewable;
import util.ColoredStringBuilder;
import util.CustomVec2D;

public class Surface extends GridSurface {
  public Surface() {
    super(Level.SIZE, 8);

    // hierarchicalNav.hierarchicalGraph.createEdgesWithinLevel(levelNr, surface);
  }

  @Override
  public String toString() {
    ColoredStringBuilder csb = new ColoredStringBuilder();
    Set<CustomVec2D> frontiers = new HashSet<>(getFrontier());
    // Add row by row to the StringBuilder
    for (int y = 0; y < hierarchicalMap.size.height; y++) {
      for (int x = 0; x < hierarchicalMap.size.width; x++) {
        // Get tile, if it doesn't know the type it is not know or void.
        CustomVec2D pos = new CustomVec2D(x, y);
        Tile t = getTile(pos);

        // Null tile
        if (t == null) {
          csb.append(Color.RESET).append(' ');
          continue;
        }

        boolean isFrontier = frontiers.contains(pos);
        boolean isVisible = t instanceof Viewable && ((Viewable) t).getVisibility();
        boolean isSeen = t.getSeen();
        boolean isShop = t instanceof Shop && ((Shop) t).isShop();

        String colorString;
        if (t instanceof Secret && ((Secret) t).isSecret()) {
          colorString = Color.TRANSPARENT.toString();
        } else if (isFrontier) {
          colorString = "\033[103m";
        } else if (isVisible) {
          colorString = "\033[0;32m";
        } else if (isShop) {
          colorString = "\033[0;31m";
        } else if (isSeen) {
          colorString = "\033[0;34m";
        } else {
          colorString = Color.RESET.toString();
        }

        csb.append(colorString).append(t.toChar());
      }

      // Don't add line after last row
      if (y != hierarchicalMap.size.height - 1) {
        csb.newLine();
      }
    }
    return csb.toString();
  }
}
