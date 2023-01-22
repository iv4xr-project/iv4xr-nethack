package agent;

import agent.navigation.NavUtils;
import agent.navigation.NetHackSurface;
import agent.navigation.surface.Wall;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.IntVec2D;
import eu.iv4xr.framework.spatial.Vec3;
import agent.navigation.NavTactic;
import nethack.object.Command;
import nethack.object.EntityType;
import agent.navigation.surface.Tile;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static nl.uu.cs.aplib.AplibEDSL.*;

public class Actions {
    static final Logger logger = LogManager.getLogger(AgentLoggers.GoalLogger);

    /**
     * Construct an action that would interact with an entity of the given id. The
     * action's guard is left unconstrained (so the action would always be enabled).
     * You can use the "on" method to add a guard.
     */
    static Action interact(String targetId) {
        return action("interact").do2((AgentState S) -> (Tile nextTile) -> {
            WorldModel newwom = NavUtils.moveTo(S, nextTile);
            logger.info(String.format(">>> interact %s", nextTile));
            return new Pair<>(S, newwom);
        });
    }

    // Construct an action that would attack an adjacent monster.
    static Action attackMonster() {
        return action("attack").do1((AgentState S) -> {
            List<WorldEntity> ms = S.adjacentEntities(EntityType.MONSTER, true);
            // just choose the first one:
            Vec3 position = ms.get(0).position;
            logger.info(String.format(">>> attackMonster %s", position));
            WorldModel newwom = NavUtils.moveTo(S, position);
            return new Pair<>(S, newwom);
        });
    }

    // Construct an action that would kick the door.
    static Action kickDoor() {
        return action("kick door").do1((AgentState S) -> {
            List<WorldEntity> ms = S.adjacentEntities(EntityType.DOOR, false);
            Vec3 position = ms.get(0).position;
            logger.info(String.format(">>> kickDoor @%s", position));
            WorldModel newwom = WorldModels.kickDoor(S, position);
            return new Pair<>(S, newwom);
        });
    }

    static Action singleAction(Command command) {
        return action(String.format("perform command: %s", command)).do1((AgentState S) -> {
            logger.info(String.format(">>> command: %s", command));
            WorldModel newwom = WorldModels.performCommand(S, command);
            return new Pair<>(S, newwom);
        });
    }

    static Action searchWalls() {
        return action("search").do2((AgentState S) -> (List<Wall> walls) -> {
            logger.info(">>> searchWalls");
            WorldModel newwom = WorldModels.performCommand(S, Command.COMMAND_SEARCH);
            for (Wall wall: walls) {
                wall.timesSearched++;
            }
            return new Pair<>(S, newwom);
        }).on((AgentState S) -> {
            NetHackSurface surface = S.multiLayerNav.areas.get((int)S.worldmodel.position.z);
            IntVec2D[] neighbours = NetHackSurface.physicalNeighbourCoordinates(NavUtils.loc2(S.worldmodel.position));
            List<Wall> walls = new ArrayList<>();
            for (IntVec2D neighbour: neighbours) {
                Tile t = surface.getTile(neighbour);
                if (t instanceof Wall) {
                    Wall wall = (Wall) t;
                    if (wall.timesSearched < 10) {
                        walls.add(wall);
                    }
                }
            }
            if (walls.size() == 0) {
                return null;
            }
            return walls;
        });
    }
}
