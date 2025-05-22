package indi.yunherry.weather.renderer;

import indi.yunherry.weather.ParticleRegistry;
import indi.yunherry.weather.WeatherType;
import indi.yunherry.weather.annotation.Renderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.concurrent.ThreadLocalRandom;

import static indi.yunherry.weather.WorldContext.nowWeather;

@Renderer
public class SnowRenderer extends WeatherRenderer {
    @Override
    public void renderWeather(LightTexture texture, float partialTick, int ticks) {
    }

    private static int PARTICLE_RADIUS = 25;
    private static int MAX_PARTICLES = 80; // 减少总数但提升有效生成率
    private static double CENTER_BIAS = 2.5; // 中心分布强度
    private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    @Override
    public void tick() {
        if (!level.isRaining()) return;
        Biome biome = this.mc.level.getBiome(camPos).value();
        //坏了,这里到底是不是冗余代码
        if (biome.getPrecipitationAt(camPos) == Biome.Precipitation.SNOW) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            for (int i = 0; i < MAX_PARTICLES; i++) {
                // 生成偏向中心的分布
                double theta = random.nextDouble() * 2 * Math.PI;
                double phi = Math.acos(Math.pow(random.nextDouble(), CENTER_BIAS)); // 使用幂次分布

                // 转换为笛卡尔坐标（Y轴向上）
                double radius1 = PARTICLE_RADIUS * Math.pow(random.nextDouble(), 0.6); // 半径向中心偏移
                double x = radius1 * Math.sin(phi) * Math.cos(theta);
                double z = radius1 * Math.sin(phi) * Math.sin(theta);
                double y = radius1 * Math.cos(phi); // 半球只需正值

                // 应用位置偏移
                double posX = x + camPos.getX();
                double posY = y + camPos.getY() + 5; // 从玩家上方5格开始
                double posZ = z + camPos.getZ();

                // 快速高度检查
                if (posY < level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY() || posY > camPos.getY() + PARTICLE_RADIUS) {
                    continue;
                }
                spawnParticle(level, level.getBiome(pos), posX, posY + random.nextFloat(), posZ);
            }
        }
    }

    private static void spawnParticle(ClientLevel level, Holder<Biome> biome, double x, double y, double z) {
        if (particleCount > maxParticleCount) {
            return;
        }
        //从云的高度下生成
        y = y + 8;
        final BlockPos getPrecipitationFromBlockPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos);
        if (ThreadLocalRandom.current().nextFloat() < 0.8f) {
            level.addParticle(ParticleRegistry.SNOW.get(), x, y, z, 0, 0, 0);
        }
    }

    @Override
    public void render() {

    }
}
