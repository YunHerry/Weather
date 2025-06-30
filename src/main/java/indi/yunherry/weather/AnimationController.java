package indi.yunherry.weather;

import indi.yunherry.weather.renderer.FogRenderer$Weather;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class AnimationController {
    private static final Logger log = LoggerFactory.getLogger(AnimationController.class);
    private static boolean isRainingPreTick = false;
    private static boolean isRaining = false;
    private static float partialTick = 0;
    private static final long TICK_NANOS = 50_000_000L;
    /**
     * 最近一次逻辑 tick 开始的绝对时间（纳秒）
     */
    private static volatile long lastTickStart = System.nanoTime();

    /**
     * 渲染帧或其它线程调用，返回 0‑1 之间的 partialTick
     */
    public static float getPartialTick() {
        long now = System.nanoTime();
        double delta = (now - lastTickStart) / 1_000_000_000.0; // 秒

        return Mth.clamp((float) (delta / 0.05), 0.0f, 1.0f);
    }
    public static void setLastTickStart(long tickStart) {
        lastTickStart = tickStart;
    }
    public static void update(Level level) {
        if (Objects.isNull(level)) return;
        //如果上一个tick于这一个tick的状态不符,则重置tick
        boolean isRainingNow = level.isRaining();
        if (isRainingPreTick != isRaining) {
            //判断是开始下雨还是结束下雨
            partialTick = level.isRaining() ? 0 : 1;

        }
        //如果是开始下雨,那么就将partialTick+0.1 反之-0.1
        // 雾浓度控制逻辑

        if (isRainingNow && partialTick <= 1f) {
            partialTick = Math.min(partialTick + 0.025f, 1f);
        } else if (!isRainingNow && partialTick > 0f) {
            partialTick = Math.max(partialTick - 0.025f, 0f);
        }
        if (partialTick != 0) {
            isRaining = true;
            //下雨结束
        } else {
            isRaining = false;
        }
        isRainingPreTick = level.isRaining();
    }

    public static boolean isRaining() {
        return isRaining;
    }

    public static float getAnimationTick() {
        return partialTick;
    }
}
