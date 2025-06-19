package indi.yunherry.weather.renderer;

import indi.yunherry.weather.annotation.Renderer;
import net.minecraft.client.renderer.LightTexture;

@Renderer
public class FogRenderer$Weather extends WeatherRenderer {
    private boolean isRainingPreTick = false;
    private static boolean isRaining = false;
    private float partialTick = 0;

    @Override
    public void renderWeather(LightTexture texture, float partialTick, int ticks) {

    }

    @Override
    public void tick() {
        //如果上一个tick于这一个tick的状态不符,则重置tick
        boolean isRaining = level.isRaining();
        if (isRainingPreTick != isRaining) {
            //判断是开始下雨还是结束下雨
            partialTick = level.isRaining() ? 0 : 1;

        }
        //如果是开始下雨,那么就将partialTick+0.1 反之-0.1
        // 雾浓度控制逻辑

        if (isRaining && partialTick <= 1f) {
            partialTick = Math.min(partialTick + 0.025f, 1f); // 雾慢慢加浓
        } else if (!isRaining && partialTick > 0f) {
            partialTick = Math.max(partialTick - 0.025f, 0f);  // 雾慢慢减淡
        }
        //开始下雨
        if (partialTick != 0) {
            FogRenderer$Weather.isRaining = true;
            //下雨结束
        } else {
            FogRenderer$Weather.isRaining = false;
        }
        isRainingPreTick = level.isRaining();

    }

    public static boolean isShouldRunning() {
        return false;
//        return isRaining;
    }

    public float getPartialTick() {
        return partialTick;
    }

    @Override
    public void render() {

    }
}
