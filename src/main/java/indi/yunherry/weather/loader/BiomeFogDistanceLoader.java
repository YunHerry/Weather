package indi.yunherry.weather.loader;

import indi.yunherry.weather.GlobalContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

import javax.annotation.Nullable;

import static indi.yunherry.weather.GlobalContext.level;

public class BiomeFogDistanceLoader {

    private static final BiomeFogTransition lastBiomeFog = new BiomeFogTransition();
    private static final BiomeFogTransition lastLiquidFog = new BiomeFogTransition();


    @Nullable
    public static FogState modifyBiomeFog(float originalNearPlane, float originalFarPlane) {
        LoaderConfig loaderConfig = GlobalContext.getLoaderConfig();
        return modifyFogParameters(level, originalNearPlane, originalFarPlane, getFogRadius((float) loaderConfig.rain(), loaderConfig.camPos().y, level.getBrightness(LightLayer.SKY, BlockPos.containing(loaderConfig.camPos()))), getFogFade((float) loaderConfig.rain(), loaderConfig.camPos().y, (int) loaderConfig.renderDistance()), lastBiomeFog);
    }

    @Nullable
    public static FogState modifyFluidFog(float originalNearPlane, float originalFarPlane, float fogRadius, float fogFade) {
        return modifyFogParameters(Minecraft.getInstance().level, originalNearPlane, originalFarPlane, fogRadius, fogFade, lastLiquidFog);
    }


    @Nullable
    private static FogState modifyFogParameters(@Nullable Level level, float originalNearPlane, float originalFarPlane, float fogRadius, float fogFade, BiomeFogTransition old) {
        if (level == null) return null;
        Float radiusMult = null;
        Float fadeMul = null;
        radiusMult = fogRadius;
        fadeMul = fogFade;

        //interpolation
        if (radiusMult == null && (Mth.abs(old.fadeMultiplier - 1) > 0.02f || Mth.abs(old.radiusMultiplier - 1) > 0.02f)) {
            radiusMult = 1f;
            fadeMul = 1f;
        }
        if (radiusMult != null) {
            float deltaTime = GlobalContext.frameTime;
            float interpolationFactor = deltaTime * 0.1f;

            // Interpolate towards the fogScalars values
            old.fadeMultiplier = Mth.lerp(interpolationFactor, old.fadeMultiplier, fadeMul);
            old.radiusMultiplier = Mth.lerp(interpolationFactor, old.radiusMultiplier, radiusMult);
            //fogEvent.scaleNearPlaneDistance(1);
            float distance = originalFarPlane - originalNearPlane;

            return new FogState((originalFarPlane - distance * old.fadeMultiplier) * old.radiusMultiplier, originalFarPlane * old.radiusMultiplier);
        }
        return null;
    }


    public record FogState(float start, float end) {}

    private static class BiomeFogTransition {
        private float fadeMultiplier = 1;
        private float radiusMultiplier = 1;
    }

    public static float getFogRadius(float rain, double playerY, int skyLight) {
        return (float) (1.0 - rain * 0.9 + ((rain > 0 && playerY < 47) ? rain * 0.9 : 0) + (playerY < 47 ? 0.2 : 0) - ((playerY <= -54 && skyLight == 0) ? 0.75 : 0));
    }

    public static float getFogFade(float rain, double playerY, int renderDistance) {
        return (float) ((1.1 + (renderDistance / 64.0) * 0.5 + rain * 0.25 - ((rain > 0 && playerY < 47) ? rain * 0.25 : 0) - (playerY < 47 ? 0.1 : 0)) * (7.5 + (renderDistance / 64.0 * 2.5)));
    }

}
