package agent.navigation;

import agent.AgentLoggers;
import agent.navigation.surface.*;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.IntVec2D;
import agent.AgentState;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static nl.uu.cs.aplib.AplibEDSL.action;

public class NavAction {
    static final Logger logger = LogManager.getLogger(AgentLoggers.NavLogger);

    /**
     * Construct an action that would guide the agent to the given location.
     */
    public static Action navigateTo(int levelId, int x, int y) {
        return action("move-to").do2((AgentState S) -> (Tile nextTile) -> {
            logger.info(String.format(">>> navigateTo %s", nextTile));
            WorldModel newwom = NavUtils.moveTo(S, nextTile);
            return new Pair<>(S, newwom);
        }).on((AgentState S) -> {
            WorldEntity agent = S.worldmodel.elements.get(S.worldmodel().agentId);
            List<Pair<Integer, Tile>> path = NavUtils.adjustedFindPath(S, NavUtils.levelNr(agent), NavUtils.loc2(S.worldmodel.position), levelId, NavUtils.loc2(x, y));
            if (path == null) {
                return null;
            }
            // the first element is the src itself, so we need to pick the next one:
            return path.get(1).snd;
        });
    }

    // Construct an action that would guide the agent to the target entity.
    public static Action navigateTo(String targetId) {
        return action("move-to").do2((AgentState S) -> (Tile nextTile) -> {
            logger.info(String.format(">>> navigateTo %s", nextTile));
            WorldModel newwom = NavUtils.moveTo(S, nextTile);
            return new Pair<>(S, newwom);
        }).on((AgentState S) -> {
            // return two possible values:
            // (1) null --> the action is not enabled
            // (2) a singleton array of tile --> the next tile to move to
            WorldEntity a = S.worldmodel.elements.get(S.worldmodel().agentId);
            IntVec2D agentPos = NavUtils.toTile(S.worldmodel.position).pos;
            WorldEntity e = S.worldmodel.elements.get(targetId);
            if (e == null) {
                logger.debug("Cannot navigate since it is nextdoor");
                return null;
            }
            List<Pair<Integer, Tile>> path = NavUtils.adjustedFindPath(S, NavUtils.levelNr(a), agentPos, NavUtils.levelNr(e), NavUtils.loc2(e.position));
            if (path == null) {
                logger.debug("No path apparently");
                return null;
            }
            logger.debug("Found path");
            return path.get(1).snd;
        });
    }

    public static Action navigateTo() {
        return action("move-to").do2((AgentState S) -> (Tile nextTile) -> {
            logger.info(String.format(">>> navigateTo %s", nextTile));
            WorldModel newwom = NavUtils.moveTo(S, nextTile);
            return new Pair<>(S, newwom);
        });
    }

    // Construct an action that would guide the agent to to the target entity.
    public static Action navigateNextTo(String targetId, boolean allowDiagonally) {
        return action("move-to").do2((AgentState S) -> (Tile nextTile) -> {
            logger.info(String.format(">>> navigateNextTo %s", nextTile));
            WorldModel newwom = NavUtils.moveTo(S, nextTile);
            return new Pair<>(S, newwom);
        }).on((AgentState S) -> {
            // return two possible values:
            // (1) null --> the action is not enabled
            // (2) a singleton array of tile --> the next tile to move to
            WorldEntity a = S.worldmodel.elements.get(S.worldmodel().agentId);
            Tile agentPos = NavUtils.toTile(S.worldmodel.position);
            WorldEntity e = S.worldmodel.elements.get(targetId);
            if (e == null) {
                logger.debug("Cannot navigate since it is nextdoor");
                return null;
            }
            Tile target = NavUtils.toTile(e.position);
            if (S.nextToEntity(targetId, allowDiagonally)) {
                logger.debug(String.format("Next to item id:%s", targetId));
                return null;
            }
            List<Pair<Integer, Tile>> path = NavUtils.adjustedFindPath(S, NavUtils.levelNr(a), NavUtils.loc2(S.worldmodel.position), NavUtils.levelNr(e), NavUtils.loc2(e.position));
            if (path == null) {
                logger.debug("No path apparently");
                return null;
            }
            logger.debug("Found path");
            return path.get(1).snd;
        });
    }


    /**
     * Construct an action that would explore the world, in the direction of the
     * given location.
     */
    public static Action explore(Pair<Integer, Tile> heuristicLocation) {
        return action("explore").do2((AgentState S) -> (Tile nextTile) -> {
            WorldModel newwom = NavUtils.moveTo(S, nextTile);
            logger.info(String.format(">>> explore %s", nextTile));
            return new Pair<>(S, newwom);
        }).on((AgentState S) -> {
            // return two possible values:
            // (1) null --> the action is not enabled
            // (2) a singleton array of tile --> the next tile to move to
            WorldEntity a = S.worldmodel.elements.get(S.worldmodel().agentId);
            Tile agentPos = NavUtils.toTile(S.worldmodel.position);
            List<Pair<Integer, Tile>> path;
            if (heuristicLocation == null) {
                path = S.multiLayerNav.explore(NavUtils.loc3(NavUtils.levelNr(a), NavUtils.loc2(S.worldmodel.position)));
            } else {
                path = S.multiLayerNav.explore(NavUtils.loc3(NavUtils.levelNr(a), NavUtils.loc2(S.worldmodel.position)), heuristicLocation);
            }
            if (path == null) {
                return null;
            }
            try {
                return path.get(1).snd;
            } catch (Exception e) {
                logger.debug(String.format("agent @%s nothing left to explore", agentPos));
                throw e;
            }
        });
    }
}
