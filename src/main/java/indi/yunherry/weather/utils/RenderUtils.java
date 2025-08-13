package indi.yunherry.weather.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.joml.Vector3f;

public class RenderUtils {
    public static Vector3f getBiomeColor(Level level, BlockPos camPos) {
        Holder<Biome> biomeHolder = level.getBiome(camPos);
        int color = biomeHolder.value().getWaterColor();
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        return new Vector3f(r, g, b);
    }
}
