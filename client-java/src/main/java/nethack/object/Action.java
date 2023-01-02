package nethack.object;

import java.util.HashMap;
import java.util.Map;

// Actions listed at: /python-server/lib/nle/nle/nethack/actions.py
public enum Action {
//	Direction	
	DIRECTION_N(0, "k", "Move N"),
	DIRECTION_E(1, "l", "Move E"),
	DIRECTION_S(2, "j", "Move S"),
	DIRECTION_W(3, "h", "Move W"),
	DIRECTION_NE(4, "u", "Move NE"),
	DIRECTION_SE(5, "n", "Move SE"),
	DIRECTION_SW(6, "b", "Move SW"),
	DIRECTION_NW(7, "y", "Move NW"),

	DIRECTION_LONG_N(8, "K", "Run N"),
	DIRECTION_LONG_E(9, "L", "Run E"),
	DIRECTION_LONG_S(10, "J", "Run S"),
	DIRECTION_LONG_W(11, "H", "Run W"),
	DIRECTION_LONG_NE(12, "U", "Run NE"),
	DIRECTION_LONG_SE(13, "N", "Run SE"),
	DIRECTION_LONG_SW(14, "B", "Run SW"),
	DIRECTION_LONG_NW(15, "Y", "Run NW"),
	
//  Misc
	MISC_UP(16, "<", "Up stairs"),
	MISC_DOWN(17, ">", "Down stairs"),
	MISC_WAIT(18, ".", "Wait"),
	MISC_MORE(19, " ", "Next message"),

//  Commands
	COMMAND_EXTCMD(20, "#", "Longer cmd"),
    COMMAND_EXTLIST(21, "?", "This window"),
    COMMAND_ADJUST(22, "#a", "Adjust inv letters"),
    COMMAND_ANNOTATE(23, "#A", "Name level"),
    COMMAND_APPLY(24, "a", "Use a tool"),
    COMMAND_ATTRIBUTES(25, "^x", "Show attributes"),
    COMMAND_AUTOPICKUP(26, "@", "Toggle autopickup"),
    COMMAND_CALL(27, "C", "Give name"),
    COMMAND_CAST(28, "Z", "Cast spell"),
    COMMAND_CHAT(29, "#c", "Chat w/ someone"),
    COMMAND_CLOSE(30, "c", "Close door"),
    COMMAND_CONDUCT(31, "#C", ""),
    COMMAND_DIP(32, "#d", "Dip object"),
    COMMAND_DROP(33, "d", "Drop item"),
    COMMAND_DROPTYPE(34, "D", "Drop itemtype"),
    COMMAND_EAT(35, "e", "Eat something"),
    COMMAND_ENGRAVE(36, "E", "Engrave floor"),
    COMMAND_ENHANCE(37, "#e", ""),
    COMMAND_ESC(38, "^[", "Esc key"),
    COMMAND_FIGHT(39, "F", "Force fight"),
    COMMAND_FIRE(40, "f", "Fire quiver"),
    COMMAND_FORCE(41, "#f", "Force lock"),
    COMMAND_GLANCE(42, ";", "Show symbol info"),
    COMMAND_HISTORY(43, "V", "Show version"),
    COMMAND_INVENTORY(44, "i", "Show inventory"),
    COMMAND_INVENTTYPE(45, "I", "Show inventory itemtypes"),
    COMMAND_INVOKE(46, "#i", "Invoke special power"),
    COMMAND_JUMP(47, "#j", "Jump location"),
    COMMAND_KICK(48, "^d", "Kick"),
    COMMAND_KNOWN(49, "\\", ""),
    COMMAND_KNOWNCLASS(50, "`", ""),
    COMMAND_LOOK(51, ":", "Look"),
    COMMAND_LOOT(52, "#l", "Loot box"),
    COMMAND_MONSTER(53, "#m", "Use monster special"),
    COMMAND_MOVE(54, "m", "Prefix: move without pickup"),
    COMMAND_MOVEFAR(55, "M", "Prefix: run"),
    COMMAND_OFFER(56, "#o", "Offer"),
    COMMAND_OPEN(57, "o", "Open door"),
    COMMAND_OPTIONS(58, "O", "Option settings"),
    COMMAND_OVERVIEW(59, "^o", "Dungeon overview"),
    COMMAND_PAY(60, "p", "Pay shopkeeper"),
    COMMAND_PICKUP(61, ",", "Pickup item"),
    COMMAND_PRAY(62, "#p", "Pray gods"),
    COMMAND_PUTON(63, "P", "Wear accesory"),
    COMMAND_QUAFF(64, "q", "Drink"),
    COMMAND_QUIT(65, "#q", "Exit unsaved"),
    COMMAND_QUIVER(66, "Q", "Select quiver ammo"),
    COMMAND_READ(67, "r", "Read"),
    COMMAND_REDRAW(68, "^r", "Redraw"),
    COMMAND_REMOVE(69, "R", "Remove accessory"),
    COMMAND_RIDE(70, "#R", "(Dis)mount monster"),
    COMMAND_RUB(71, "#r", "Rub lamp/stone"),
    COMMAND_RUSH(72, "g", ""),
    COMMAND_RUSH2(73, "G", ""),
    COMMAND_SAVE(74, "S", "Save game"),
    COMMAND_SEARCH(75, "s", "Search secret"),
    COMMAND_SEEALL(76, "*", "Show equipment"),
    COMMAND_SEEAMULET(77, "\"", "Show amulet"),
    COMMAND_SEEARMOR(78, "[", "Show armor"),
    COMMAND_SEEGOLD(79, "$", "Show gold"),
    COMMAND_SEERINGS(80, "=", "Show rings"),
    COMMAND_SEESPELLS(81, "+", "Show spells"),
    COMMAND_SEETOOLS(82, "(", "Show tools"),
    COMMAND_SEETRAP(83, "#^", "Show traptype"),
    COMMAND_SEEWEAPON(84, ")", "Show weapon"),
    COMMAND_SHELL(85, "!", ""),
    COMMAND_SIT(86, "#s", "Sit"),
    COMMAND_SWAP(87, "x", "Swap primary"),
    COMMAND_TAKEOFF(88, "T", "Empty container"),
    COMMAND_TAKEOFFALL(89, "A", "Unwear armor"),
    COMMAND_TELEPORT(90, "^t", "Teleport"),
    COMMAND_THROW(91, "t", "Throw"),
    COMMAND_TIP(92, "#T", "Empty container"),
    COMMAND_TRAVEL(93, "_", "Travel"),
    COMMAND_TURN(94, "#t", ""),
    COMMAND_TWOWEAPON(95, "X", "Two-weaponed"),
    COMMAND_UNTRAP(96, "#u", "Untrap"),
    COMMAND_VERSION(97, "#v", "Version & Compile"),
    COMMAND_VERSIONSHORT(98, "v", "Version"),
    COMMAND_WEAR(99, "W", "Wear armor"),
    COMMAND_WHATDOES(100, "&", "Tell command info"),
    COMMAND_WHATIS(101, "/", "Show symbol meaning"),
    COMMAND_WIELD(102, "w", "Wield weapon"),
    COMMAND_WIPE(103, "#w", "Wipe face"),
    COMMAND_ZAP(104, "z", "Use wand"),
	
//  Characters    
    TEXTCHARACTER_PLUS(105, "%+", "Show spells"),
    TEXTCHARACTER_MINUS(106, "%-", ""),
    TEXTCHARACTER_SPACE(107, "% ", ""),
    TEXTCHARACTER_APOS(108, "%\'", ""),
    TEXTCHARACTER_QUOTE(109, "%\"", ""),
    TEXTCHARACTER_NUM_0(110, "%0", ""),
    TEXTCHARACTER_NUM_1(111, "%1", ""),
    TEXTCHARACTER_NUM_2(112, "%2", ""),
    TEXTCHARACTER_NUM_3(113, "%3", ""),
    TEXTCHARACTER_NUM_4(114, "%4", ""),
    TEXTCHARACTER_NUM_5(115, "%5", ""),
    TEXTCHARACTER_NUM_6(116, "%6", ""),
    TEXTCHARACTER_NUM_7(117, "%7", ""),
    TEXTCHARACTER_NUM_8(118, "%8", ""),
    TEXTCHARACTER_NUM_9(119, "%9", ""),
    TEXTCHARACTER_DOLLAR(120, "%$", "");
    
	public int index;
	
	private String stroke;
	private String description;
	private static final Map<String, Action> BY_STROKE = new HashMap<>();
	
	Action(int index, String stroke, String description) {
		this.index = index;
		this.stroke = stroke;
		this.description = description;
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
    }

	public static void prettyPrintActions() {
		int n = Action.values().length;
		int rowLength = 21;
		
		String[] ActionStrs = new String[n];
		int[] nrValues = new int[(n / rowLength) + 1];
		
		// Convert array of actions to array of 
		for (int i = 0; i < n; i++) {
			Action action = Action.values()[i];
			ActionStrs[i] = String.format("%3d %-2s %s", action.index, action.stroke, action.description);
			nrValues[i / rowLength] = Math.max(ActionStrs[i].length(), nrValues[i / rowLength]);
		}

		// Print each row of command with descriptions
		for (int i = 0; i < rowLength; i++) {
			for (int j = 0; j < (n / rowLength) + 1; j++) {
				int index = j * rowLength + i;
				if (index >= n) {
					break;
				}
				
				if (j != 0) {
					System.out.print('|');
				}
				String s = ActionStrs[index];
				System.out.print(s);
				System.out.print(" ".repeat(nrValues[j] - s.length()));
			}
			System.out.println();
		}
	}
}
