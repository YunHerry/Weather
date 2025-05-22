package indi.yunherry.weather.renderer;

import indi.yunherry.weather.ParticleRegistry;
import indi.yunherry.weather.WindDirectionType;
import indi.yunherry.weather.WorldContext;
import indi.yunherry.weather.annotation.Renderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;

import java.util.concurrent.ThreadLocalRandom;

import static indi.yunherry.weather.WorldContext.windDirection;

@Renderer(isConditionalRendering = true, isEnableRandomTick = true)
public class WindRenderer extends ParticleRenderer {
    @Override
    public void tick() {

    }

    @Override
    public void randomTick() {
        WindDirectionType[] directionTypes = WindDirectionType.values();
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
        Biome biome = this.mc.level.getBiome(camPos).value();
        if (level.isRaining() && biome.getPrecipitationAt(camPos) != Biome.Precipitation.SNOW) return;
        if (windDirection != WindDirectionType.NONE) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            level.addParticle(ParticleRegistry.WIND.get(), camPos.getX() + random.nextDouble() - 0.5, camPos.getY() + random.nextDouble() - 0.6, camPos.getZ() + random.nextDouble() - 0.5, 0.0, 0.0, 0.0);
//                this.mc.level.addParticle(ParticleRegistry.WIND.get(), 517, 94, 210, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public boolean isRender() {
        return ThreadLocalRandom.current().nextInt(100) >= 99;
    }
}
