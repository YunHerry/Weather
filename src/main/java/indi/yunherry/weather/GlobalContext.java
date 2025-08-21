package indi.yunherry.weather;

import indi.yunherry.weather.loader.LoaderConfig;
import indi.yunherry.weather.renderer.ParticleRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;

public abstract class GlobalContext {
    public static BlockPos camPos;
    public static ClientLevel level;
    public static Minecraft mc;

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
}
