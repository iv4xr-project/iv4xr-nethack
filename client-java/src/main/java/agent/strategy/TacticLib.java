package agent.strategy;

import static agent.selector.ItemSelector.rangedWeapon;
import static nl.uu.cs.aplib.AplibEDSL.*;

import agent.iv4xr.AgentState;
import agent.navigation.strategy.NavTactic;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.Tile;
import agent.navigation.surface.Walkable;
import agent.selector.EntitySelector;
import agent.selector.ItemSelector;
import agent.selector.MonsterSelector;
import java.util.*;
import nethack.enums.CommandEnum;
import nethack.enums.Skill;
import nethack.object.Command;
import nethack.object.Monster;
import nethack.object.Player;
import nethack.object.items.FoodItem;
import nethack.object.items.Item;
import nethack.object.items.WeaponItem;
import nl.uu.cs.aplib.mainConcepts.Tactic;
import nl.uu.cs.aplib.utils.Pair;
import util.CustomVec2D;
import util.CustomVec3D;

/**
 * TacticLib Provide several basic actions and tactics.
 *
 * <p>Keep in mind that the provided navigation and exploration tactics/goals currently has no
 * ability to deal with items that block a corridor. The solution is for now to just generate
 * another dungeon where we have no corridors are not blocked by items (use another random seed, for
 * example). A better fix would be to have a smarter navigation and exploration. TO DO.
 *
 * @author wish
 */
public class TacticLib {
  public static Tactic attackMonsters() {
    return FIRSTof(
        Actions.wield()
            .on(
                (AgentState S) -> {
                  List<Item> meleeWeapons =
                      ItemSelector.meleeWeapon.filter(
                          Arrays.asList(S.app().gameState.player.inventory.items), S);

                  // Has no melee weapon or already has one equipped
                  if (meleeWeapons.isEmpty()
                      || meleeWeapons.stream()
                          .anyMatch(meleeWeapon -> ((WeaponItem) meleeWeapon).wielded)) {
                    return null;
                  }

                  // An angry enemy is close-by
                  List<Monster> monsters =
                      MonsterSelector.aggressive.filter(S.app().level().monsters, S);
                  CustomVec2D agentPos = S.loc().pos;
                  // No enemies close by don't act
                  if (monsters.stream()
                      .noneMatch(monster -> CustomVec2D.manhattan(monster.pos, agentPos) >= 2)) {
                    return null;
                  }

                  // Wield this melee weapon
                  return meleeWeapons.get(0);
                })
            .lift(),
        Actions.attack()
            .on(
                (AgentState S) -> {
                  Monster monster =
                      MonsterSelector.adjacentAggressive.apply(S.app().level().monsters, S);
                  if (monster == null) {
                    return null;
                  }
                  return NavUtils.toDirection(S, monster.loc);
                })
            .lift(),
        Actions.wield()
            .on(
                (AgentState S) -> {
                  List<Item> rangedWeapons =
                      rangedWeapon.filter(
                          Arrays.asList(S.app().gameState.player.inventory.items), S);

                  boolean hasCrossbowAmmo =
                      rangedWeapons.stream()
                          .anyMatch(
                              rangedWeapon ->
                                  rangedWeapon.entityInfo.skill == Skill.CROSSBOW
                                      && rangedWeapon.entityInfo.missile);
                  if (hasCrossbowAmmo) {
                    Optional<Item> crossbow =
                        rangedWeapons.stream()
                            .filter(
                                ranged ->
                                    ranged.entityInfo.skill == Skill.CROSSBOW
                                        && !ranged.entityInfo.missile)
                            .findFirst();

                    if (crossbow.isPresent()) {
                      if (crossbow.get().description.contains("weapon in hand")) {
                        return null;
                      } else {
                        return crossbow.get();
                      }
                    }
                  }

                  boolean hasBowAmmo =
                      rangedWeapons.stream()
                          .anyMatch(
                              rangedWeapon ->
                                  rangedWeapon.entityInfo.skill == Skill.BOW
                                      && rangedWeapon.entityInfo.missile);
                  if (hasBowAmmo) {
                    Optional<Item> bow =
                        rangedWeapons.stream()
                            .filter(
                                ranged ->
                                    ranged.entityInfo.skill == Skill.BOW
                                        && !ranged.entityInfo.missile)
                            .findFirst();

                    if (bow.isPresent()) {
                      if (bow.get().description.contains("weapon in hand")) {
                        return null;
                      } else {
                        return bow.get();
                      }
                    }
                  }

                  // Dagger doesn't need to be readied
                  if (rangedWeapons.stream()
                      .anyMatch(rangedWeapon -> rangedWeapon.entityInfo.skill == Skill.DAGGER)) {
                    return null;
                  }

                  // Has no ranged weapon, so subsequently also cannot fire
                  return null;
                })
            .lift(),
        Actions.fire()
            .on(
                (AgentState S) -> {
                  Item fireItem = null;
                  List<Item> rangedWeapons =
                      rangedWeapon.filter(
                          Arrays.asList(S.app().gameState.player.inventory.items), S);

                  boolean hasCrossbowAmmo =
                      rangedWeapons.stream()
                          .anyMatch(
                              rangedWeapon ->
                                  rangedWeapon.entityInfo.skill == Skill.CROSSBOW
                                      && rangedWeapon.entityInfo.missile);
                  if (hasCrossbowAmmo) {
                    Optional<Item> crossbow =
                        rangedWeapons.stream()
                            .filter(
                                ranged ->
                                    ranged.entityInfo.skill == Skill.CROSSBOW
                                        && !ranged.entityInfo.missile)
                            .findFirst();

                    if (crossbow.isPresent()) {
                      fireItem = crossbow.get();
                    }
                  }

                  if (fireItem == null) {
                    boolean hasBowAmmo =
                        rangedWeapons.stream()
                            .anyMatch(
                                rangedWeapon ->
                                    rangedWeapon.entityInfo.skill == Skill.BOW
                                        && rangedWeapon.entityInfo.missile);
                    if (hasBowAmmo) {
                      Optional<Item> bow =
                          rangedWeapons.stream()
                              .filter(
                                  ranged ->
                                      ranged.entityInfo.skill == Skill.BOW
                                          && !ranged.entityInfo.missile)
                              .findFirst();

                      if (bow.isPresent()) {
                        fireItem = bow.get();
                      }
                    }
                  }

                  if (fireItem == null) {
                    Optional<Item> maybeDagger =
                        rangedWeapons.stream()
                            .filter(rangedWeapon -> rangedWeapon.entityInfo.skill == Skill.DAGGER)
                            .findFirst();

                    // Character has a dagger to throw
                    if (maybeDagger.isPresent()) {
                      fireItem = maybeDagger.get();
                    }
                  }

                  // No ranged items with ammo, cannot fire
                  if (fireItem == null) {
                    return null;
                  }

                  // Only count aggressive monsters that are close enough to the agent
                  CustomVec3D agentLoc = S.loc();
                  List<Monster> monsters =
                      MonsterSelector.aggressive.filter(S.app().level().monsters, S);
                  monsters =
                      new MonsterSelector()
                          .predicate(
                              (monster, agentState) ->
                                  CustomVec2D.manhattan(monster.pos, agentLoc.pos) < 6)
                          .filter(monsters, S);
                  if (monsters.isEmpty()) {
                    return null;
                  }

                  // Get closest entity
                  Monster closestMonster = null;
                  int manhattanDistance = Integer.MAX_VALUE;

                  outer:
                  for (Monster monster : monsters) {
                    int xSign = Integer.signum(monster.pos.x - agentLoc.pos.x);
                    int ySign = Integer.signum(monster.pos.y - agentLoc.pos.y);
                    CustomVec2D delta = new CustomVec2D(xSign, ySign);
                    CustomVec2D currentPos = agentLoc.pos.add(delta);
                    int currentDistance = 1;

                    while (!currentPos.equals(monster.pos)) {
                      Tile tile = S.area().getTile(currentPos);
                      if (!(tile instanceof Walkable) || !((Walkable) tile).isWalkable()) {
                        continue outer;
                      }
                      currentPos = currentPos.add(delta);
                      currentDistance += 1;
                      if (currentDistance >= manhattanDistance) {
                        continue outer;
                      }
                    }

                    closestMonster = monster;
                    manhattanDistance = currentDistance;
                  }

                  // Too far away to reliably hit
                  if (manhattanDistance > 6) {
                    return null;
                  }

                  CustomVec2D monsterPos = closestMonster.pos;
                  int xSign = Integer.signum(monsterPos.x - agentLoc.pos.x);
                  int ySign = Integer.signum(monsterPos.y - agentLoc.pos.y);
                  CustomVec2D delta = new CustomVec2D(xSign, ySign);
                  return new Pair<>(
                      fireItem,
                      NavUtils.toDirection(
                          S, new CustomVec3D(agentLoc.lvl, agentLoc.pos.add(delta))));
                })
            .lift());
  }

  public static Tactic resolveHungerState(int prayerTimeOut) {
    return FIRSTof(
        Actions.pray()
            .on(
                (AgentState S) -> {
                  Player player = S.app().gameState.player;
                  if (!player.hungerState.wantsFood()) {
                    return null;
                  }
                  Integer lastPrayerTurn = player.lastPrayerTurn;
                  if (lastPrayerTurn == null
                      || lastPrayerTurn - S.app().gameState.stats.turn.time > prayerTimeOut) {
                    return true;
                  }
                  return null;
                })
            .lift(),
        NavTactic.interactWorldEntity(
            EntitySelector.freshCorpse.globalPredicate(
                S -> S.app().gameState.player.hungerState.wantsFood()),
            List.of(
                new Command(CommandEnum.COMMAND_EAT),
                new Command('y'),
                new Command(CommandEnum.MISC_MORE))),
        Actions.eatItem()
            .on(
                (AgentState S) -> {
                  Player player = S.app().gameState.player;
                  // Player stomach full enough
                  if (!player.hungerState.wantsFood()) {
                    return null;
                  }
                  // Picks the food item with the lowest nutrition per weight
                  List<Item> items =
                      Arrays.stream(player.inventory.items)
                          .filter(
                              item ->
                                  item instanceof FoodItem && ((FoodItem) item).foodInfo != null)
                          .sorted(
                              Comparator.comparingDouble(
                                  item -> ((FoodItem) item).foodInfo.nutritionPerWeight))
                          .toList();
                  if (items.isEmpty()) {
                    return null;
                  }
                  return items.get(0);
                })
            .lift());
  }
}
