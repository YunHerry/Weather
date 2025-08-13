package indi.yunherry.weather;

import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 全局静态类，用于控制一个在0.0和1.0之间平滑变化的动画值。
 * 核心功能是根据世界（Level）的下雨状态，在开始下雨时让一个值缓缓增加到1.0，
 * 在雨停时缓缓减小到0.0。
 */
public class AnimationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnimationController.class);

    /**
     * 过渡动画的速度。数值越大，过渡越快。
     */
    private static final float TRANSITION_SPEED = 0.005f;

    /**
     * 当前的动画过渡值 (0.0 到 1.0)。
     */
    private static float animationValue = 0.0f;
    /**
     * 上一个游戏刻（Game Tick）的动画过渡值，用于渲染插值。
     */
    private static float prevAnimationValue = 0.0f;

    /**
     * 此方法应该在客户端的【每个游戏刻调用一次】（例如，在 ClientTickEvent 中）。
     * 它负责根据下雨状态更新动画值。
     *
     * @param level 客户端的世界（Level）对象。
     */
    public static void tick(Level level) {
        if (level == null) {
            return;
        }

        // 保存当前值，用于下一渲染帧的插值计算。
        prevAnimationValue = animationValue;

        boolean isRainingNow = level.isRaining();

        if (isRainingNow) {
            // 如果正在下雨，平滑地将动画值增加到 1.0。
            animationValue = Math.min(1.0f, animationValue + TRANSITION_SPEED);
        } else {
            // 如果没有下雨，平滑地将动画值减少到 0.0。
            animationValue = Math.max(0.0f, animationValue - TRANSITION_SPEED);
        }
    }

    /**
     * 为当前的渲染帧获取平滑的动画过渡值。
     * 这就是您需要的那个在0.0和1.0之间平滑变化的值。
     *
     * @param partialTicks 渲染器提供的、自上一个游戏刻以来经过的时间比例。
     * @return 一个平滑的、介于 0.0 和 1.0 之间的动画值。
     */
    public static float getAnimationPartialTick(float partialTicks) {
        // 使用 Mth.lerp() 进行线性插值，得到极其平滑的视觉效果。
        return Mth.lerp(partialTicks, prevAnimationValue, animationValue);
    }
    public static float getSteppedAnimationValue() {
        return animationValue;
    }
    /**
     * 检查动画当前是否处于活动状态（即值不为0）。
     *
     * @return 如果动画值大于0，则返回 true。
     */
    public static boolean isAnimationActive() {
        return animationValue > 0.001f;
    }
}