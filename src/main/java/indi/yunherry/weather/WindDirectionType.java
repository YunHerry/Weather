package indi.yunherry.weather;

public enum WindDirectionType {
    NORTH,
    WEST,
    EAST,
    SOUTH,
    NONE;
    public static String getWindDirectionType(WindDirectionType windDirection) {
        return switch (windDirection) {
            case NORTH -> "北风";
            case WEST -> "西风";
            case EAST -> "东风";
            case SOUTH -> "南风";
            case NONE -> "无风";
        };
    }
}
