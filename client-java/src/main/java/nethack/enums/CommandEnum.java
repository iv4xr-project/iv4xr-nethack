package nethack.enums;

import java.util.*;
import util.ColoredStringBuilder;

// Actions listed at: /python-server/lib/nle/nle/nethack/actions.py
public enum CommandEnum {
  // Direction
  DIRECTION_N("k", "Move N"),
  DIRECTION_E("l", "Move E"),
  DIRECTION_S("j", "Move S"),
  DIRECTION_W("h", "Move W"),
  DIRECTION_NE("u", "Move NE"),
  DIRECTION_SE("n", "Move SE"),
  DIRECTION_SW("b", "Move SW"),
  DIRECTION_NW("y", "Move NW"),

  DIRECTION_LONG_N("K", "Run N"),
  DIRECTION_LONG_E("L", "Run E"),
  DIRECTION_LONG_S("J", "Run S"),
  DIRECTION_LONG_W("H", "Run W"),
  DIRECTION_LONG_NE("U", "Run NE"),
  DIRECTION_LONG_SE("N", "Run SE"),
  DIRECTION_LONG_SW("B", "Run SW"),
  DIRECTION_LONG_NW("Y", "Run NW"),

  // Misc
  MISC_UP("<", "Up stairs"),
  MISC_DOWN(">", "Down stairs"),
  MISC_WAIT(".", "Wait"),
  MISC_MORE("~", "Next message"),

  // Commands
  COMMAND_EXTCMD("#", "Longer cmd"),
  COMMAND_EXTLIST("?", "This window"),
  COMMAND_ADJUST("#a", "Adjust inv letters"),
  COMMAND_ANNOTATE("#A", "Name level"),
  COMMAND_APPLY("a", "Use a tool"),
  COMMAND_ATTRIBUTES("^x", "Show attributes"),
  COMMAND_AUTOPICKUP("@", "Toggle autopickup"),
  COMMAND_CALL("C", "Give name"),
  COMMAND_CAST("Z", "Cast spell"),
  COMMAND_CHAT("#c", "Chat w/ someone"),
  COMMAND_CLOSE("c", "Close door"),
  COMMAND_CONDUCT("#C", ""),
  COMMAND_DIP("#d", "Dip object"),
  COMMAND_DROP("d", "Drop item"),
  COMMAND_DROPTYPE("D", "Drop itemtype"),
  COMMAND_EAT("e", "Eat something"),
  COMMAND_ENGRAVE("E", "Engrave floor"),
  COMMAND_ENHANCE("#e", ""),
  COMMAND_ESC("^[", "Esc key"),
  COMMAND_FIGHT("F", "Force fight"),
  COMMAND_FIRE("f", "Fire quiver"),
  COMMAND_FORCE("#f", "Force lock"),
  COMMAND_GLANCE(";", "Show symbol info"),
  COMMAND_HISTORY("V", "Show version"),
  COMMAND_INVENTORY("i", "Show inventory"),
  COMMAND_INVENTTYPE("I", "Show inventory itemtypes"),
  COMMAND_INVOKE("#i", "Invoke special power"),
  COMMAND_JUMP("#j", "Jump location"),
  COMMAND_KICK("^d", "Kick"),
  COMMAND_KNOWN("\\", ""),
  COMMAND_KNOWNCLASS("`", ""),
  COMMAND_LOOK(":", "Look"),
  COMMAND_LOOT("#l", "Loot box"),
  COMMAND_MONSTER("#m", "Use monster special"),
  COMMAND_MOVE("m", "Prefix: move without pickup"),
  COMMAND_MOVEFAR("M", "Prefix: run"),
  COMMAND_OFFER("#o", "Offer"),
  COMMAND_OPEN("o", "Open door"),
  COMMAND_OPTIONS("O", "Option settings"),
  COMMAND_OVERVIEW("^o", "Dungeon overview"),
  COMMAND_PAY("p", "Pay shopkeeper"),
  COMMAND_PICKUP(",", "Pickup item"),
  COMMAND_PRAY("#p", "Pray gods"),
  COMMAND_PUTON("P", "Wear accessory"),
  COMMAND_QUAFF("q", "Drink"),
  COMMAND_QUIT("#q", "Exit unsaved"),
  COMMAND_QUIVER("Q", "Select quiver ammo"),
  COMMAND_READ("r", "Read"),
  COMMAND_REDRAW("^r", "Redraw"),
  COMMAND_REMOVE("R", "Remove accessory"),
  COMMAND_RIDE("#R", "(Dis)mount monster"),
  COMMAND_RUB("#r", "Rub lamp/stone"),
  COMMAND_RUSH("g", ""),
  COMMAND_RUSH2("G", ""),
  COMMAND_SAVE("S", "Save game"),
  COMMAND_SEARCH("s", "Search secret"),
  COMMAND_SEEALL("*", "Show equipment"),
  COMMAND_SEEAMULET("\"", "Show amulet"),
  COMMAND_SEEARMOR("[", "Show armor"),
  COMMAND_SEEGOLD("$", "Show gold"),
  COMMAND_SEERINGS("=", "Show rings"),
  COMMAND_SEESPELLS("+", "Show spells"),
  COMMAND_SEETOOLS("(", "Show tools"),
  COMMAND_SEETRAP("#^", "Show trap type"),
  COMMAND_SEEWEAPON(")", "Show weapon"),
  COMMAND_SHELL("!", ""),
  COMMAND_SIT("#s", "Sit"),
  COMMAND_SWAP("x", "Swap primary"),
  COMMAND_TAKEOFF("T", "Empty container"),
  COMMAND_TAKEOFFALL("A", "Unwear armor"),
  COMMAND_TELEPORT("^t", "Teleport"),
  COMMAND_THROW("t", "Throw"),
  COMMAND_TIP("#T", "Empty container"),
  COMMAND_TRAVEL("_", "Travel"),
  COMMAND_TURN("#t", ""),
  COMMAND_TWOWEAPON("X", "Two-weaponed"),
  COMMAND_UNTRAP("#u", "Untrap"),
  COMMAND_VERSION("#v", "Version & Compile"),
  COMMAND_VERSIONSHORT("v", "Version"),
  COMMAND_WEAR("W", "Wear armor"),
  COMMAND_WHATDOES("&", "Tell command info"),
  COMMAND_WHATIS("/", "Show symbol meaning"),
  COMMAND_WIELD("w", "Wield weapon"),
  COMMAND_WIPE("#w", "Wipe face"),
  COMMAND_ZAP("z", "Use wand"),

  // Characters
  TEXT_CHARACTER_PLUS("%+", "Show spells"),
  TEXT_CHARACTER_MINUS("%-", ""),
  TEXT_CHARACTER_SPACE("% ", ""),
  TEXT_CHARACTER_APOS("%'", ""),
  TEXT_CHARACTER_QUOTE("%\"", ""),
  TEXT_CHARACTER_NUM_0("%0", ""),
  TEXT_CHARACTER_NUM_1("%1", ""),
  TEXT_CHARACTER_NUM_2("%2", ""),
  TEXT_CHARACTER_NUM_3("%3", ""),
  TEXT_CHARACTER_NUM_4("%4", ""),
  TEXT_CHARACTER_NUM_5("%5", ""),
  TEXT_CHARACTER_NUM_6("%6", ""),
  TEXT_CHARACTER_NUM_7("%7", ""),
  TEXT_CHARACTER_NUM_8("%8", ""),
  TEXT_CHARACTER_NUM_9("%9", ""),
  TEXT_CHARACTER_DOLLAR("%$", ""),

  ADDITIONAL_SHOW_SEED("@?", "Show seed"),
  ADDITIONAL_SHOW_VERBOSE("@v", "Show all information"),
  ADDITIONAL_ASCII("-▉", "Perform literal action");

  static final CommandEnum[] nethackChallengeCommands =
      new CommandEnum[] {
        DIRECTION_N,
        DIRECTION_E,
        DIRECTION_S,
        DIRECTION_W,
        DIRECTION_NE,
        DIRECTION_SE,
        DIRECTION_SW,
        DIRECTION_NW,
        DIRECTION_LONG_N,
        DIRECTION_LONG_E,
        DIRECTION_LONG_S,
        DIRECTION_LONG_W,
        DIRECTION_LONG_NE,
        DIRECTION_LONG_SE,
        DIRECTION_LONG_SW,
        DIRECTION_LONG_NW,
        MISC_UP,
        MISC_DOWN,
        MISC_WAIT,
        MISC_MORE,
        COMMAND_EXTCMD,
        COMMAND_EXTLIST,
        COMMAND_ADJUST,
        COMMAND_ANNOTATE,
        COMMAND_APPLY,
        COMMAND_ATTRIBUTES,
        COMMAND_AUTOPICKUP,
        COMMAND_CALL,
        COMMAND_CAST,
        COMMAND_CHAT,
        COMMAND_CLOSE,
        COMMAND_CONDUCT,
        COMMAND_DIP,
        COMMAND_DROP,
        COMMAND_DROPTYPE,
        COMMAND_EAT,
        COMMAND_ENGRAVE,
        COMMAND_ENHANCE,
        COMMAND_ESC,
        COMMAND_FIGHT,
        COMMAND_FIRE,
        COMMAND_FORCE,
        COMMAND_GLANCE,
        COMMAND_HISTORY,
        COMMAND_INVENTORY,
        COMMAND_INVENTTYPE,
        COMMAND_INVOKE,
        COMMAND_JUMP,
        COMMAND_KICK,
        COMMAND_KNOWN,
        COMMAND_KNOWNCLASS,
        COMMAND_LOOK,
        COMMAND_LOOT,
        COMMAND_MONSTER,
        COMMAND_MOVE,
        COMMAND_MOVEFAR,
        COMMAND_OFFER,
        COMMAND_OPEN,
        COMMAND_OPTIONS,
        COMMAND_OVERVIEW,
        COMMAND_PAY,
        COMMAND_PICKUP,
        COMMAND_PRAY,
        COMMAND_PUTON,
        COMMAND_QUAFF,
        COMMAND_QUIT,
        COMMAND_QUIVER,
        COMMAND_READ,
        COMMAND_REDRAW,
        COMMAND_REMOVE,
        COMMAND_RIDE,
        COMMAND_RUB,
        COMMAND_RUSH,
        COMMAND_RUSH2,
        COMMAND_SAVE,
        COMMAND_SEARCH,
        COMMAND_SEEALL,
        COMMAND_SEEAMULET,
        COMMAND_SEEARMOR,
        COMMAND_SEEGOLD,
        COMMAND_SEERINGS,
        COMMAND_SEESPELLS,
        COMMAND_SEETOOLS,
        COMMAND_SEETRAP,
        COMMAND_SEEWEAPON,
        COMMAND_SHELL,
        COMMAND_SIT,
        COMMAND_SWAP,
        COMMAND_TAKEOFF,
        COMMAND_TAKEOFFALL,
        COMMAND_TELEPORT,
        COMMAND_THROW,
        COMMAND_TIP,
        COMMAND_TRAVEL,
        COMMAND_TURN,
        COMMAND_TWOWEAPON,
        COMMAND_UNTRAP,
        COMMAND_VERSION,
        COMMAND_VERSIONSHORT,
        COMMAND_WEAR,
        COMMAND_WHATDOES,
        COMMAND_WHATIS,
        COMMAND_WIELD,
        COMMAND_WIPE,
        COMMAND_ZAP,
        TEXT_CHARACTER_PLUS,
        TEXT_CHARACTER_MINUS,
        TEXT_CHARACTER_SPACE,
        TEXT_CHARACTER_APOS,
        TEXT_CHARACTER_QUOTE,
        TEXT_CHARACTER_NUM_0,
        TEXT_CHARACTER_NUM_1,
        TEXT_CHARACTER_NUM_2,
        TEXT_CHARACTER_NUM_3,
        TEXT_CHARACTER_NUM_4,
        TEXT_CHARACTER_NUM_5,
        TEXT_CHARACTER_NUM_6,
        TEXT_CHARACTER_NUM_7,
        TEXT_CHARACTER_NUM_8,
        TEXT_CHARACTER_NUM_9,
        TEXT_CHARACTER_DOLLAR,
      };
  static final CommandEnum[] nethackCommands =
      new CommandEnum[] {
        DIRECTION_N,
        DIRECTION_E,
        DIRECTION_S,
        DIRECTION_W,
        DIRECTION_NE,
        DIRECTION_SE,
        DIRECTION_SW,
        DIRECTION_NW,
        DIRECTION_LONG_N,
        DIRECTION_LONG_E,
        DIRECTION_LONG_S,
        DIRECTION_LONG_W,
        DIRECTION_LONG_NE,
        DIRECTION_LONG_SE,
        DIRECTION_LONG_SW,
        DIRECTION_LONG_NW,
        MISC_UP,
        MISC_DOWN,
        MISC_WAIT,
        MISC_MORE,
        COMMAND_ADJUST,
        COMMAND_APPLY,
        COMMAND_ATTRIBUTES,
        COMMAND_CALL,
        COMMAND_CAST,
        COMMAND_CHAT,
        COMMAND_CLOSE,
        COMMAND_DIP,
        COMMAND_DROP,
        COMMAND_DROPTYPE,
        COMMAND_EAT,
        COMMAND_ENGRAVE,
        COMMAND_ENHANCE,
        COMMAND_ESC,
        COMMAND_FIGHT,
        COMMAND_FIRE,
        COMMAND_FORCE,
        COMMAND_INVENTORY,
        COMMAND_INVENTTYPE,
        COMMAND_INVOKE,
        COMMAND_JUMP,
        COMMAND_KICK,
        COMMAND_LOOK,
        COMMAND_LOOT,
        COMMAND_MONSTER,
        COMMAND_MOVE,
        COMMAND_MOVEFAR,
        COMMAND_OFFER,
        COMMAND_OPEN,
        COMMAND_PAY,
        COMMAND_PICKUP,
        COMMAND_PRAY,
        COMMAND_PUTON,
        COMMAND_QUAFF,
        COMMAND_QUIVER,
        COMMAND_READ,
        COMMAND_REMOVE,
        COMMAND_RIDE,
        COMMAND_RUB,
        COMMAND_RUSH,
        COMMAND_RUSH2,
        COMMAND_SEARCH,
        COMMAND_SEEARMOR,
        COMMAND_SEERINGS,
        COMMAND_SEETOOLS,
        COMMAND_SEETRAP,
        COMMAND_SEEWEAPON,
        COMMAND_SHELL,
        COMMAND_SIT,
        COMMAND_SWAP,
        COMMAND_TAKEOFF,
        COMMAND_TAKEOFFALL,
        COMMAND_THROW,
        COMMAND_TIP,
        COMMAND_TURN,
        COMMAND_TWOWEAPON,
        COMMAND_UNTRAP,
        COMMAND_VERSIONSHORT,
        COMMAND_WEAR,
        COMMAND_WIELD,
        COMMAND_WIPE,
        COMMAND_ZAP,
        TEXT_CHARACTER_PLUS,
        TEXT_CHARACTER_QUOTE,
        TEXT_CHARACTER_DOLLAR,
        TEXT_CHARACTER_SPACE
      };
  static final CommandEnum[] additionalCommands =
      new CommandEnum[] {ADDITIONAL_SHOW_SEED, ADDITIONAL_SHOW_VERBOSE, ADDITIONAL_ASCII};
  private static final Set<CommandEnum> commandsHandledByClient =
      new HashSet<>(
          Arrays.asList(
              COMMAND_EXTLIST,
              COMMAND_REDRAW,
              ADDITIONAL_SHOW_VERBOSE,
              ADDITIONAL_SHOW_SEED,
              COMMAND_INVENTORY,
              COMMAND_INVENTTYPE));
  private static final Map<String, CommandEnum> BY_STROKE = new HashMap<>();
  private static final Map<CommandEnum, Integer> COMMAND_TO_NETHACK_CHALLENGE_INDEX =
      new HashMap<>();
  private static final Map<CommandEnum, Integer> COMMAND_TO_NETHACK_INDEX = new HashMap<>();

  static {
    for (CommandEnum c : values()) {
      BY_STROKE.put(c.stroke, c);
    }
    for (CommandEnum c : additionalCommands) {
      BY_STROKE.put(c.stroke, c);
    }
    int i = 0;
    for (CommandEnum c : nethackChallengeCommands) {
      COMMAND_TO_NETHACK_CHALLENGE_INDEX.put(c, i++);
    }
    i = 0;
    for (CommandEnum c : nethackCommands) {
      COMMAND_TO_NETHACK_INDEX.put(c, i++);
    }
  }

  private final String description;
  public final String stroke;

  CommandEnum(String stroke, String description) {
    this.stroke = stroke;
    this.description = description;
  }

  public static CommandEnum fromValue(String stroke) {
    return BY_STROKE.getOrDefault(stroke, null);
  }

  public static void prettyPrintCommands(GameMode gameMode) {
    CommandEnum[] commands =
        gameMode == GameMode.NetHackChallenge ? nethackChallengeCommands : nethackCommands;
    int n = commands.length;
    int N = commands.length + additionalCommands.length;
    int columnLength = 21;

    String[] CommandStrs = new String[N];
    int[] nrValues = new int[N / columnLength + 1];

    // Convert array of commands to array of strings
    for (int i = 0; i < N; i++) {
      if (i < n) {
        CommandStrs[i] =
            String.format("%3d %-2s %s", i, commands[i].stroke, commands[i].description);
      } else {
        CommandStrs[i] =
            String.format(
                "--- %-2s %s",
                additionalCommands[i - n].stroke, additionalCommands[i - n].description);
      }
      nrValues[i / columnLength] = Math.max(CommandStrs[i].length(), nrValues[i / columnLength]);
    }

    ColoredStringBuilder csb = new ColoredStringBuilder();
    // Print each row of command with descriptions
    for (int i = 0; i < columnLength; i++) {
      for (int j = 0; j < (n / columnLength) + 1; j++) {
        int index = j * columnLength + i;
        if (index >= N) {
          break;
        }

        if (j != 0) {
          csb.append('|');
        }
        String s = CommandStrs[index];
        csb.append(s);
        csb.append(" ".repeat(nrValues[j] - s.length()));
      }
      csb.newLine();
    }
    System.out.print(csb);
  }

  public boolean handleByClient() {
    return commandsHandledByClient.contains(this);
  }

  public int getIndex(GameMode gameMode) {
    if (Objects.requireNonNull(gameMode) == GameMode.NetHack) {
      return COMMAND_TO_NETHACK_INDEX.getOrDefault(this, -1);
    }
    return COMMAND_TO_NETHACK_CHALLENGE_INDEX.getOrDefault(this, -1);
  }
}
