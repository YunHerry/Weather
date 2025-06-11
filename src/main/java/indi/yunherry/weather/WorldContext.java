package indi.yunherry.weather;

import indi.yunherry.weather.factory.bean.RendererEngine;
import indi.yunherry.weather.renderer.ParticleRenderer;
import indi.yunherry.weather.renderer.WeatherRenderer;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class WorldContext {
    public static boolean isDebugMode = false;
    public static WeatherType nowWeather = WeatherType.NONE;
    public static WindDirectionType windDirection = WindDirectionType.NONE;
    public static ThreadLocalRandom random = ThreadLocalRandom.current();
    public static boolean isEasing = false;
    public static float easeStartAngle = 0.0f;
    public static float easeTargetAngle = 0.0f;
    public static int easeDuration = 0;
    public static int easeTimer = 0;
    // 触发缓动的角度阈值
    public static final float TRIGGER_ANGLE = 0.0f;
    public static final float TRIGGER_RANGE = 15.0f;

    // 缓动参数
    public static final int MIN_EASE_DURATION = 20; // 最小缓动时间(tick)
    public static final int MAX_EASE_DURATION = 40; // 最大缓动时间
    public static final float MAX_EASE_OFFSET = 30.0f; // 最大角度偏移
    public static final List<RendererEngine> renderers = new ArrayList<>();
    public static final Map<String, ParticleRenderer> beans = new ConcurrentHashMap<>();
    public static Class<?> mainClass;
    protected WorldContext()
    {
    }

}
