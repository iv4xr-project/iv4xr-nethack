package nethack.enums;

import java.util.HashMap;
import java.util.Map;

public enum HungerState {
    SATURATED(1),
    HUNGRY(2),
    WEAK(3),
    FAINTING(4);

    private static final Map<Integer, HungerState> BY_VALUE = new HashMap<>();

    static {
        for (HungerState hungerState : values()) {
            BY_VALUE.put(hungerState.value, hungerState);
        }
    }

    final int value;

    HungerState(int value) {
        this.value = value;
    }

    public static HungerState fromValue(int value) {
        if (BY_VALUE.containsKey(value)) {
            return BY_VALUE.get(value);
        }
        throw new IllegalArgumentException("HungerState value not known: " + value);
    }
}
