package agent.iv4xr;

import agent.navigation.surface.Tile;
import agent.navigation.surface.Walkable;
import eu.iv4xr.framework.extensions.ltl.LTL;
import nethack.NetHack;
import nethack.enums.HungerState;
import nl.uu.cs.aplib.mainConcepts.SimpleState;
import util.CustomVec2D;
import util.CustomVec3D;

import static eu.iv4xr.framework.extensions.ltl.LTL.always;

public class AgentLTL {
  public static LTL<SimpleState> scoreIncreasing() {
    return always((SimpleState S) -> {
      NetHack netHack = ((AgentState)S).app();

      // Score may not be negative
      if (netHack.gameState.stats.score < 0) {
        return false;
      }

      // Make sure the score increases
      if (netHack.previousGameState == null) {
        return true;
      } else {
        return netHack.gameState.stats.score >= netHack.previousGameState.stats.score;
      }
    });
  }

  public static LTL<SimpleState> hp() {
    return always((SimpleState S) -> {
      NetHack netHack = ((AgentState)S).app();

      if (netHack.gameState.player.hp < 0) {
        return false;
      }

      return netHack.gameState.player.hp <= netHack.gameState.player.hpMax;
    });
  }

  public static LTL<SimpleState> energy() {
    return always((SimpleState S) -> {
      NetHack netHack = ((AgentState)S).app();

      if (netHack.gameState.player.energy < 0) {
        return false;
      }

      return netHack.gameState.player.energy <= netHack.gameState.player.energyMax;
    });
  }

  public static LTL<SimpleState> lvlIncreasing() {
    return always((SimpleState S) -> {
      NetHack netHack = ((AgentState)S).app();

      // Score may not be negative
      if (netHack.gameState.stats.levelNumber < 0) {
        return false;
      }

      // Make sure the score increases
      if (netHack.previousGameState == null) {
        return true;
      } else {
        return netHack.gameState.stats.levelNumber >= netHack.previousGameState.stats.levelNumber;
      }
    });
  }

  public static LTL<SimpleState> hungerState() {
    return always((SimpleState S) -> {
      NetHack netHack = ((AgentState)S).app();

      if (netHack.previousGameState == null) {
        return true;
      }

      HungerState hungerState = netHack.gameState.player.hungerState;
      HungerState prevHungerState = netHack.previousGameState.player.hungerState;

      //
      if (hungerState == prevHungerState) {
        return true;
      } else if (hungerState.compareTo(prevHungerState) < 0) {
        return true;
      } else {
        return hungerState.ordinal() - prevHungerState.ordinal() == 1;
      }
    });
  }

  public static LTL<SimpleState> experienceIncreasing() {
    return always((SimpleState S) -> {
      NetHack netHack = ((AgentState)S).app();

      if (netHack.gameState.player.experienceLevel < 1 || netHack.gameState.player.experiencePoints < 0) {
        return false;
      }

      if (netHack.previousGameState == null) {
        return true;
      }

      if (netHack.gameState.player.experienceLevel < netHack.previousGameState.player.experienceLevel) {
        return false;
      }
      return netHack.gameState.player.experiencePoints >= netHack.previousGameState.player.experiencePoints;
    });
  }

  public static LTL<SimpleState> turnIncreasing() {
    return always((SimpleState S) -> {
      NetHack netHack = ((AgentState)S).app();

      if (netHack.gameState.stats.turn.time < 0) {
        return false;
      }

      if (netHack.previousGameState == null) {
        return true;
      }
      return netHack.gameState.stats.turn.time >= netHack.previousGameState.stats.turn.time;
    });
  }

  public static LTL<SimpleState> walkable() {
    return always((SimpleState S) -> {
      NetHack netHack = ((AgentState)S).app();
      Tile tile = netHack.gameState.getLevel().surface.getTile(netHack.gameState.player.location.pos);
      if (!(tile instanceof Walkable)) {
        return false;
      }

      return ((Walkable)tile).isWalkable();
    });
  }

  public static LTL<SimpleState> adjacent() {
    return always((SimpleState S) -> {
      NetHack netHack = ((AgentState)S).app();

      if (netHack.previousGameState == null) {
        return true;
      }

      CustomVec3D prevLoc = netHack.previousGameState.player.location;
      CustomVec3D loc = netHack.gameState.player.location;

      if (prevLoc.equals(loc) || prevLoc.lvl != loc.lvl) {
        return true;
      }

      return CustomVec2D.adjacent(prevLoc.pos, loc.pos, true);
    });
  }
}
