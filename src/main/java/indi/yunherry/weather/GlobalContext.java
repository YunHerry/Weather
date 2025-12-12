package indi.yunherry.weather;

import indi.yunherry.weather.loader.LoaderConfig;
import indi.yunherry.weather.renderer.ParticleRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class GlobalContext {
    public static BlockPos camPos;
    public static ClientLevel level;
    public static Minecraft mc;
    public static final Map<String, String> DEBUG_VALUES = new LinkedHashMap<>();
    private static int renderDistance;
    public static float frameTime;
    private static float rain;
    private static float skyLight;
    private static LoaderConfig loaderConfig;
    public static void update() {
        Minecraft mc = Minecraft.getInstance();
        ParticleRenderer.mc = mc;
        camPos = mc.gameRenderer.getMainCamera().getBlockPosition();
        level = mc.level;
        //debug
        DEBUG_VALUES.clear();
        if (level != null) {
            DEBUG_VALUES.put("Minecraft Time", String.format("%.0f / 24000", (float)level.getDayTime() % 24000));
        }
        DEBUG_VALUES.put("Rain/Thunder Level", String.format("%.2f", rain));
        DEBUG_VALUES.put("Sky Light", String.format("%d", (int)skyLight));
        DEBUG_VALUES.put("Camera Y", String.format("%.2f", camPos.getCenter().y));

        renderDistance = mc.options.renderDistance().get();
        frameTime = mc.getFrameTime();
        rain = level.getRainLevel(frameTime) * 0.5f + level.getThunderLevel(frameTime) * 0.5f;
        skyLight = level.getBrightness(LightLayer.SKY, camPos);
//        AnimationController.tick(level);

        loaderConfig = LoaderConfig.builder().rain(rain).camPos(camPos.getCenter()).skyLight((int) skyLight).renderDistance(renderDistance).build();
    }

    public static LoaderConfig getLoaderConfig() {
        return loaderConfig;
    }
    public static void setDebugEnvironmentalVal(String key, String debugVal) {
        DEBUG_VALUES.put(key, debugVal);
    }
    public static String getDebugEnvironmentalVal(String key) {
        return DEBUG_VALUES.get(key);
    }
}
