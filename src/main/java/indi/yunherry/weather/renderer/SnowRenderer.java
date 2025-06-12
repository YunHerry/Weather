package indi.yunherry.weather.renderer;

import indi.yunherry.weather.WeatherConfig;
import indi.yunherry.weather.WorldContext;
import indi.yunherry.weather.annotation.Renderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.concurrent.ThreadLocalRandom;

@Renderer
public class SnowRenderer extends WeatherRenderer {
    @Override
    public void renderWeather(LightTexture texture, float partialTick, int ticks) {
    }

    public static int PARTICLE_RADIUS = 25;
    public static int MAX_PARTICLES = 80;
    public static double CENTER_BIAS = 2.5;
    private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    @Override
    public void tick() {
        if (!level.isRaining()) return;
        Biome biome = this.mc.level.getBiome(camPos).value();
        if (biome.getPrecipitationAt(camPos) == Biome.Precipitation.SNOW) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            for (int i = 0; i < MAX_PARTICLES; i++) {
                // 生成偏向中心的分布
                double theta = random.nextDouble() * 2 * Math.PI;
                double phi = Math.acos(Math.pow(random.nextDouble(), CENTER_BIAS)); // 使用幂次分布

                double x = WeatherConfig.RENDER_RADIUS * Math.sin(phi) * Math.cos(theta);
                double z = WeatherConfig.RENDER_RADIUS * Math.sin(phi) * Math.sin(theta);
                double y = WeatherConfig.RENDER_RADIUS * Math.cos(phi); // 半球只需正值

                // 应用位置偏移
                double posX = x + camPos.getX();
                double posY = y + camPos.getY() + 5; // 从玩家上方5格开始
                double posZ = z + camPos.getZ();

                // 快速高度检查
                if (posY < level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY() || posY > camPos.getY() + PARTICLE_RADIUS) {
                    continue;
                }
//                spawnParticle(level, level.getBiome(pos), posX+ random.nextFloat(), posY + random.nextFloat(), posZ+ random.nextFloat());
                if (particleCount > maxParticleCount) {
                    return;
                }
                if (ThreadLocalRandom.current().nextFloat() < 0.8f) {
                    level.addParticle(WorldContext.particleBeans.get("snow").get(), posX + random.nextFloat(), posY + random.nextFloat(), posZ + random.nextFloat(), 0, 0, 0);
                }
            }
        }
    }


    @Override
    public void render() {

    }
}
