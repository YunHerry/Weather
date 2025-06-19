package indi.yunherry.weather;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "weather")
public class WeatherConfig implements ConfigData {
    public static int RENDER_RADIUS = 25;
}
