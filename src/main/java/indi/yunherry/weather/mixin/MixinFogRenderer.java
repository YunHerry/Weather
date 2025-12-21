package indi.yunherry.weather.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import indi.yunherry.weather.GlobalContext;
import indi.yunherry.weather.loader.*;
import indi.yunherry.weather.utils.ColorMapUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CubicSampler;

import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = FogRenderer.class, priority = 1001)
public abstract class MixinFogRenderer {
    @Shadow private static float fogRed;
    @Shadow private static float fogGreen;
    @Shadow
    private static float fogBlue;
    @Unique
    private static Vec3 weather$currentFogColor;

    @Inject(
            method = "setupFog",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V",
                    shift = At.Shift.BEFORE
            )
    )
    private static void modifyFogDistance(
            Camera camera,
            FogRenderer.FogMode fogMode,
            float farPlaneDistance,
            boolean p_234176_,
            float partialTick,
            CallbackInfo ci,
            @Local(argsOnly = false) FogRenderer.FogData fogData
    ) {
        FogType fogType = camera.getFluidInCamera();
        if (fogMode == FogRenderer.FogMode.FOG_TERRAIN && fogType == FogType.NONE) {
            BiomeFogDistanceLoader.FogState targetFog =
                    BiomeFogDistanceLoader.modifyBiomeFog(fogData.start, fogData.end);

            if (targetFog != null) {
                fogData.start = targetFog.start();
                fogData.end = targetFog.end();
            }
        }
    }

    @WrapOperation(
            method = "setupColor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/CubicSampler;gaussianSampleVec3(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/util/CubicSampler$Vec3Fetcher;)Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private static Vec3 weather$modifyFogColor(
            Vec3 center,
            CubicSampler.Vec3Fetcher fetcher,
            Operation<Vec3> original,
            @Local(argsOnly = true) Camera camera,
            @Local(argsOnly = true) ClientLevel level,
            @Local(argsOnly = true) int renderDistanceChunks,
            @Local(ordinal = 6) float lightLevel
    ) {
        Vec3 camPos = GlobalContext.camPos.getCenter();
        FogType fogtype = camera.getFluidInCamera();

        Vec3 targetColor = null;

        ResourceLocation biomeRL = ColorMapUtils.getAccurateBiomeID(
                level,
                BlockPos.containing(camPos)
        );
        BiomeFogColorLoader loader = LoaderManager.getLoader(
                BiomeFogColorLoader.BIOME_FOG_COLOR_LOADER,
                BiomeFogColorLoader.class
        );

        if (loader != null) {
            LoaderConfig loaderConfig = LoaderConfig.builder()
                    .rain(level.getRainLevel(Minecraft.getInstance().getFrameTime()))
                    .skyLight((int) lightLevel)
                    .camPos(camPos)
                    .renderDistance(renderDistanceChunks)
                    .build();

            Vector4f rgba = loader.findColorByKey(biomeRL.toString(), loaderConfig);
            if (rgba != null) {
                targetColor = new Vec3(rgba.x, rgba.y, rgba.z);
            }
        }


        if (targetColor == null) {
            targetColor = original.call(center, fetcher);
        }

        if (weather$currentFogColor == null) {
            weather$currentFogColor = targetColor;
        } else {
            double factor = (fogtype == FogType.WATER || fogtype == FogType.LAVA) ? 0.1 : 0.02;
            weather$currentFogColor = weather$currentFogColor.lerp(targetColor, factor);
        }

        return weather$currentFogColor;
    }

    @Inject(method = "setupColor", at = @At("RETURN"))
    private static void weather$modifyWaterFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistanceChunks, float bossColorModifier, CallbackInfo ci) {
        FogType fogType = camera.getFluidInCamera();
        // 只有在水中时才执行
        if (fogType == FogType.WATER) {
            Vec3 targetColor = null;
            Vec3 camPos = GlobalContext.camPos.getCenter();
            ResourceLocation biomeRL = ColorMapUtils.getAccurateBiomeID(level, BlockPos.containing(camPos));

            BiomeWaterFogColorLoader loader = LoaderManager.getLoader(
                    BiomeWaterFogColorLoader.BIOME_WATER_FOG_COLOR_LOADER,
                    BiomeWaterFogColorLoader.class
            );

            if (loader != null) {
                // 如果是黑色测试，可以直接 new Vec3(0,0,0)
                int colorInt = loader.getColorMapByString(biomeRL.toString());
                Vector4f rgba = ColorMapUtils.int2Vector4fColor(colorInt);
                targetColor = new Vec3(rgba.x, rgba.y, rgba.z);
            }

            // 如果我们获取到了自定义颜色
            if (targetColor != null) {
                // 应用平滑 (注意：由于这是 RETURN 注入，我们需要小心处理 currentFogColor 的状态)
                // 这里为了演示简单，直接复用 static 变量，建议根据实际情况决定是否要和陆地雾共用同一个 lerp 变量
                // 水下的颜色变化通常比较快，factor 可以设大一点，比如 0.1

                if (weather$currentFogColor == null) {
                    weather$currentFogColor = targetColor;
                } else {
                    weather$currentFogColor = weather$currentFogColor.lerp(targetColor, 0.1);
                }

                fogRed = (float) weather$currentFogColor.x;
                fogGreen = (float) weather$currentFogColor.y;
                fogBlue = (float) weather$currentFogColor.z;
            }
        }
    }
}