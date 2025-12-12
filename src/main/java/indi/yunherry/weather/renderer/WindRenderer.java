package indi.yunherry.weather.renderer;

import indi.yunherry.weather.Sounds;
import indi.yunherry.weather.WindDirectionType;
import indi.yunherry.weather.WorldContext;
import indi.yunherry.weather.annotation.Renderer;
import indi.yunherry.weather.factory.factory.RendererFactory;
import indi.yunherry.weather.utils.SoundUtils;
import net.minecraft.util.Mth; // 引入 Mth 用于插值计算
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

import static indi.yunherry.weather.WorldContext.windDirection;

@Renderer(isConditionalRendering = true, isEnableRandomTick = true)
public class WindRenderer extends ParticleRenderer {
    private static final Logger log = LoggerFactory.getLogger(RendererFactory.class);
    private final WindDirectionType[] directionTypes = WindDirectionType.values();

    // --- 新增的动画控制变量 ---

    /**
     * 上一个游戏刻的风向，用于检测风向是否发生变化。
     */
    private WindDirectionType lastWindDirection = WindDirectionType.NONE;

    /**
     * 当前风向过渡动画的值 (范围 0.0 到 1.0)。
     */
    private float windTransition = 0.0f;

    /**
     * 上一个游戏刻的过渡动画值，用于平滑插值。
     */
    private float prevWindTransition = 0.0f;

    /**
     * 标记当前是否正在进行过渡动画。
     */
    private boolean isTransitioning = false;

    /**
     * 过渡动画的速度，数值越大，动画越快。
     */
    private static final float TRANSITION_SPEED = 0.05f; // 可按需调整

    // --- 修改后的 tick 方法 ---

    @Override
    public void tick() {
        // 1. 为渲染插值做准备：保存上一刻的动画值
        this.prevWindTransition = this.windTransition;

        // 2. 检测风向是否发生变化
        if (windDirection != this.lastWindDirection) {
            // 如果风向变了，启动或重置过渡动画
            this.isTransitioning = true;
            this.windTransition = 0.0f; // 动画从0开始
            log.info("Wind direction changed from {} to {}. Starting transition.", this.lastWindDirection, windDirection);
            // 更新最后记录的风向
            this.lastWindDirection = windDirection;
            SoundUtils.playWindInDirection(windDirection, 20, 40f, 1.0f);
        }

        // 3. 如果正在播放动画，则更新动画进度
        if (this.isTransitioning) {
            this.windTransition = Math.min(1.0f, this.windTransition + TRANSITION_SPEED);
            // 如果动画播放完毕 (值达到1.0)，则停止
            if (this.windTransition >= 1.0f) {
                this.isTransitioning = false;
                log.info("Wind transition finished.");
            }
        }

        // 4. 处理粒子效果，并与动画关联
        // 如果没下雨，并且当前有风，则尝试生成粒子
        if (level != null && !level.isRaining() && windDirection != WindDirectionType.NONE) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            // 将粒子生成概率与动画过渡值关联
            // windTransition 从 0 -> 1，生成概率从 0% -> 8%
            int spawnChance = (int) (8 * this.windTransition);
            if (random.nextInt(100) < spawnChance) {
                level.addParticle(
                        WorldContext.particleBeans.get("wind").get(),
                        camPos.getX() + random.nextDouble(-10.0, 10.0), // 扩大粒子生成范围
                        camPos.getY() + random.nextDouble(-2.0, 2.0),
                        camPos.getZ() + random.nextDouble(-10.0, 10.0),
                        0.0, 0.0, 0.0);
            }
        }
    }


    /**
     * 获取平滑的风力过渡动画值 (partialTick)。
     * 其他渲染器或逻辑可以调用此方法来获取一个0到1之间平滑变化的值，以实现同步的动画效果。
     *
     * @param partialTicks 渲染引擎提供的部分刻（代表当前帧在两个游戏刻之间的位置）
     * @return 介于 0.0 和 1.0 之间的平滑动画值。
     */
    public float getSmoothWindTransition(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevWindTransition, this.windTransition);
    }


    // --- 原有方法保持不变 ---

    @Override
    public void randomTick() {
        // 这个方法现在是触发风向变化的“扳机”
        if (windDirection != WindDirectionType.NONE) {
            windDirection = WindDirectionType.NONE;
        } else {
            windDirection = directionTypes[ThreadLocalRandom.current().nextInt(directionTypes.length)];
        }
    }

    @Override
    public boolean isRandomTick() {
        return ThreadLocalRandom.current().nextInt(1000) >= 998;
    }

    @Override
    public void render() {
    }

    @Override
    public boolean isRender() {
        return false;
    }
}