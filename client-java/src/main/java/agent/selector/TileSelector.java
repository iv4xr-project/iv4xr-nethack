package agent.selector;

import agent.AgentState;
import agent.navigation.NavUtils;
import agent.navigation.NetHackSurface;
import agent.navigation.surface.Tile;
import agent.navigation.surface.Wall;
import agent.selector.EntitySelector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.IntVec2D;
import nethack.object.EntityType;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TileSelector extends Selector<Tile> {
    Class tileClass;
    public static final TileSelector wallSelector = new TileSelector(SelectionType.CLOSEST, Wall.class, t -> { Wall w = (Wall)t; return w.timesSearched < 10;});
    public TileSelector(SelectionType selectionType, Class tileClass, Predicate<Tile> predicate) {
        super(selectionType, predicate);
        this.tileClass = tileClass;
    }

    public TileSelector(SelectionType selectionType, Class tileClass) {
        super(selectionType);
        this.tileClass = tileClass;
    }

    @Override
    public Tile apply(List<Tile> tiles, AgentState S) {
        return select(filter(tiles), S);
    }

    @Override
    public Tile select(List<Tile> tiles, AgentState S) {
        if (selectionType == SelectionType.FIRST || selectionType == SelectionType.LAST) {
            return super.select(tiles, S);
        }

        int n = tiles.size();
        // Goes wrong for multiple levels
        IntVec2D agentPos = NavUtils.loc2(S.worldmodel.position);
        float min = NetHackSurface.distSq(agentPos, tiles.get(0).pos);
        float max = min;
        int minIndex = 0, maxIndex = 0;
        for (int i = 1; i < n; i++) {
            float dist = NetHackSurface.distSq(agentPos, tiles.get(i).pos);
            if (dist < min) {
                min = dist;
                minIndex = i;
            } else if (dist > max) {
                max = dist;
                maxIndex = i;
            }
        }

        if (selectionType == SelectionType.CLOSEST) {
            return tiles.get(minIndex);
        } else if (selectionType == SelectionType.FARTHEST) {
            return tiles.get(maxIndex);
        } else {
            throw new UnknownError("SelectionType not implemented: " + selectionType);
        }
    }

    private List<Tile> filter(List<Tile> tiles) {
        Stream<Tile> stream;
        if (tileClass != null && predicate != null) {
            stream = tiles.stream().filter(t -> Objects.equals(tileClass, t.getClass()) && predicate.test(t));
        } else if (tileClass != null) {
            stream = tiles.stream().filter(t -> Objects.equals(tileClass, t.getClass()));
        } else if (predicate != null) {
            stream = tiles.stream().filter(t -> predicate.test(t));
        } else {
            stream = tiles.stream();
        }
        return stream.collect(Collectors.toList());
    }
}