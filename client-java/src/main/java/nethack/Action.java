package nethack;

import java.util.HashMap;
import java.util.Map;

// Actions listed at: /python-server/lib/nle/nle/nethack/actions.py
public enum Action {
//	Direction	
	DIRECTION_N(0, "k"),
	DIRECTION_E(1, "l"),
	DIRECTION_S(2, "j"),
	DIRECTION_W(3, "n"),
	DIRECTION_NE(4, "u"),
	DIRECTION_SE(5, "n"),
	DIRECTION_SW(6, "b"),
	DIRECTION_NW(7, "y"),

	DIRECTION_LONG_N(8, "K"),
	DIRECTION_LONG_E(9, "L"),
	DIRECTION_LONG_S(10, "J"),
	DIRECTION_LONG_W(11, "H"),
	DIRECTION_LONG_NE(12, "U"),
	DIRECTION_LONG_SE(13, "N"),
	DIRECTION_LONG_SW(14, "B"),
	DIRECTION_LONG_NW(15, "Y"),
	
//  Misc
	MISC_UP(16, "<"),
	MISC_DOWN(17, ">"),
	MISC_WAIT(18, "."),
	MISC_MORE(19, " "),

//  Commands
	COMMAND_EXTCMD(20, "#"),
    COMMAND_EXTLIST(21, "#?"),
    COMMAND_ADJUST(22, "#a"),
    COMMAND_ANNOTATE(23, "#A"),
    COMMAND_APPLY(24, "a"),
    COMMAND_ATTRIBUTES(25, "^x"),
    COMMAND_AUTOPICKUP(26, "@"),
    COMMAND_CALL(27, "C"),
    COMMAND_CAST(28, "Z"),
    COMMAND_CHAT(29, "#c"),
    COMMAND_CLOSE(30, "c"),
    COMMAND_CONDUCT(31, "#C"),
    COMMAND_DIP(32, "#d"),
    COMMAND_DROP(33, "d"),
    COMMAND_DROPTYPE(34, "D"),
    COMMAND_EAT(35, "e"),
    COMMAND_ENGRAVE(36, "E"),
    COMMAND_ENHANCE(37, "#e"),
    COMMAND_ESC(38, "^["),
    COMMAND_FIGHT(39, "F"),
    COMMAND_FIRE(40, "f"),
    COMMAND_FORCE(41, "#f"),
    COMMAND_GLANCE(42, ";"),
    COMMAND_HISTORY(43, "V"),
    COMMAND_INVENTORY(44, "i"),
    COMMAND_INVENTTYPE(45, "I"),
    COMMAND_INVOKE(46, "#i"),
    COMMAND_JUMP(47, "#j"),
    COMMAND_KICK(48, "^d"),
    COMMAND_KNOWN(49, "\\"),
    COMMAND_KNOWNCLASS(50, "`"),
    COMMAND_LOOK(51, ":"),
    COMMAND_LOOT(52, "#l"),
    COMMAND_MONSTER(53, "#m"),
    COMMAND_MOVE(54, "m"),
    COMMAND_MOVEFAR(55, "M"),
    COMMAND_OFFER(56, "#o"),
    COMMAND_OPEN(57, "o"),
    COMMAND_OPTIONS(58, "O"),
    COMMAND_OVERVIEW(59, "^o"),
    COMMAND_PAY(60, "p"),
    COMMAND_PICKUP(61, ","),
    COMMAND_PRAY(62, "#p"),
    COMMAND_PUTON(63, "P"),
    COMMAND_QUAFF(64, "q"),
    COMMAND_QUIT(65, "#q"),
    COMMAND_QUIVER(66, "Q"),
    COMMAND_READ(67, "r"),
    COMMAND_REDRAW(68, "^r"),
    COMMAND_REMOVE(69, "R"),
    COMMAND_RIDE(70, "#R"),
    COMMAND_RUB(71, "#r"),
    COMMAND_RUSH(72, "g"),
    COMMAND_RUSH2(73, "G"),
    COMMAND_SAVE(74, "S"),
    COMMAND_SEARCH(75, "s"),
    COMMAND_SEEALL(76, "*"),
    COMMAND_SEEAMULET(77, "\""),
    COMMAND_SEEARMOR(78, "["),
    COMMAND_SEEGOLD(79, "$"),
    COMMAND_SEERINGS(80, "="),
    COMMAND_SEESPELLS(81, "+"),
    COMMAND_SEETOOLS(82, "("),
    COMMAND_SEETRAP(83, "#^"),
    COMMAND_SEEWEAPON(84, ")"),
    COMMAND_SHELL(85, "!"),
    COMMAND_SIT(86, "#s"),
    COMMAND_SWAP(87, "x"),
    COMMAND_TAKEOFF(88, "T"),
    COMMAND_TAKEOFFALL(89, "A"),
    COMMAND_TELEPORT(90, "^t"),
    COMMAND_THROW(91, "t"),
    COMMAND_TIP(92, "#T"),
    COMMAND_TRAVEL(93, "_"),
    COMMAND_TURN(94, "#t"),
    COMMAND_TWOWEAPON(95, "X"),
    COMMAND_UNTRAP(96, "#u"),
    COMMAND_VERSION(97, "#v"),
    COMMAND_VERSIONSHORT(98, "v"),
    COMMAND_WEAR(99, "W"),
    COMMAND_WHATDOES(100, "&"),
    COMMAND_WHATIS(101, "/"),
    COMMAND_WIELD(102, "w"),
    COMMAND_WIPE(103, "#w"),
    COMMAND_ZAP(104, "z"),
	
//  Characters    
    TEXTCHARACTER_PLUS(105, "%+"),
    TEXTCHARACTER_MINUS(106, "%-"),
    TEXTCHARACTER_SPACE(107, "% "),
    TEXTCHARACTER_APOS(108, "%\'"),
    TEXTCHARACTER_QUOTE(109, "%\""),
    TEXTCHARACTER_NUM_0(110, "%0"),
    TEXTCHARACTER_NUM_1(111, "%1"),
    TEXTCHARACTER_NUM_2(112, "%2"),
    TEXTCHARACTER_NUM_3(113, "%3"),
    TEXTCHARACTER_NUM_4(114, "%4"),
    TEXTCHARACTER_NUM_5(115, "%5"),
    TEXTCHARACTER_NUM_6(116, "%6"),
    TEXTCHARACTER_NUM_7(117, "%7"),
    TEXTCHARACTER_NUM_8(118, "%8"),
    TEXTCHARACTER_NUM_9(119, "%9"),
    TEXTCHARACTER_DOLLAR(120, "%$");
    
	private String stroke;
	public int index;
	private static final Map<String, Action> BY_STROKE = new HashMap<>();
	
	Action(int index, String stroke) {
		this.index = index;
		this.stroke = stroke;
	}
	
	static {
        for (Action a: values()) {
        	BY_STROKE.put(a.stroke, a);
        }
    }
	
	public static Action fromValue(String stroke) {
		if (BY_STROKE.containsKey(stroke)) {
			return BY_STROKE.get(stroke);
		}
		
		return null;
//		throw new IllegalArgumentException("Action key input not known: " + value);
    }

	public static void printActions()
	{
		for (Action a: Action.values()) {
		    System.out.println("i:" + a.index + " " + a.name() + " stroke:" + a.stroke);
		}
	}
}
