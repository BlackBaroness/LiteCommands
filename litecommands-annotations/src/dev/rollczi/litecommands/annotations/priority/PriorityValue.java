package dev.rollczi.litecommands.annotations.priority;

import dev.rollczi.litecommands.priority.PriorityLevel;

public final class PriorityValue {

    public static final int NONE = Integer.MIN_VALUE;
    public static final int LOWEST = -1000;
    public static final int VERY_LOW = -500;
    public static final int LOW = -100;
    public static final int BELOW_NORMAL = -50;
    public static final int NORMAL = 0;
    public static final int ABOVE_NORMAL = 50;
    public static final int HIGH = 100;
    public static final int VERY_HIGH = 500;
    public static final int HIGHEST = 1000;
    public static final int MAX = Integer.MAX_VALUE;

    private PriorityValue() {
    }

    public static PriorityLevel toPriorityLevel(int value) {
        switch (value) {
            case NONE:
                return PriorityLevel.NONE;
            case LOWEST:
                return PriorityLevel.LOWEST;
            case VERY_LOW:
                return PriorityLevel.VERY_LOW;
            case LOW:
                return PriorityLevel.LOW;
            case BELOW_NORMAL:
                return PriorityLevel.BELOW_NORMAL;
            case NORMAL:
                return PriorityLevel.NORMAL;
            case ABOVE_NORMAL:
                return PriorityLevel.ABOVE_NORMAL;
            case HIGH:
                return PriorityLevel.HIGH;
            case VERY_HIGH:
                return PriorityLevel.VERY_HIGH;
            case HIGHEST:
                return PriorityLevel.HIGHEST;
            case MAX:
                return PriorityLevel.MAX;
            default:
                return new PriorityLevel("CUSTOM-" + value, value);
        }
    }

}