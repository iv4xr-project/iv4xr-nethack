package nethack.utils;

import eu.iv4xr.framework.extensions.pathfinding.LayeredAreasNavigation;
import nethack.GameState;
import nethack.object.Level;
import nethack.utils.NethackSurface_NavGraph.Tile;

public class RenderUtils {
    private GameState gameState;
    private LayeredAreasNavigation<Tile, NethackSurface_NavGraph> layeredGraph;

    public RenderUtils(GameState gameState, LayeredAreasNavigation<Tile, NethackSurface_NavGraph> layeredGraph) {
        this.gameState = gameState;
        this.layeredGraph = layeredGraph;
    }

    public void render() {
        NethackSurface_NavGraph layer = layeredGraph.areas.get(gameState.stats.zeroIndexLevelNumber);

        String[] navigation = layer.toString().split(System.lineSeparator());
        String[] game = gameState.toString().split(System.lineSeparator());

        System.out.println(game[0]);

        for (int i = 0; i < Level.HEIGHT; i++) {
            System.out.println(game[i + 1] + " " + navigation[i]);
        }

        System.out.println(game[Level.HEIGHT + 1]);
        System.out.println(game[Level.HEIGHT + 2]);
    }
}