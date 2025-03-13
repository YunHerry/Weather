package indi.yunherry.weather;

import indi.yunherry.weather.renderer.WeatherRenderer;
import net.minecraft.client.Minecraft;

public class WorldContext {
    public final static WeatherRenderer renderer = WeatherRenderer.instance;
    public static String nowWeather = "rain";
    protected WorldContext()
    {
    }
}
