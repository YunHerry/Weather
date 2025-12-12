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

import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = FogRenderer.class, priority = 1001)
public abstract class MixinFogRenderer {

    @Unique
    private static Vec3 weather$currentFogColor;
    /**
     * @author
     * @reason
     */
// 我们不再使用 @Overwrite，而是使用 @Inject
    // 注入点选择在 RenderSystem.setShaderFogStart 被调用之前
    // 这样我们可以在雾气应用到渲染系统之前，最后一次修改它
    @Inject(
            method = "setupFog",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V",
                    shift = At.Shift.BEFORE // 在调用前插入
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
        if (fogMode == FogRenderer.FogMode.FOG_TERRAIN) {
            BiomeFogDistanceLoader.FogState targetFog = BiomeFogDistanceLoader.modifyBiomeFog(fogData.start, fogData.end);

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
    private static Vec3 weather$modifyFogColor(Vec3 center, CubicSampler.Vec3Fetcher fetcher,
                                               Operation<Vec3> original,
                                               @Local(argsOnly = true) Camera camera,
                                               @Local(argsOnly = true) ClientLevel level,
                                               @Local(argsOnly = true) int renderDistanceChunks,
                                               @Local(ordinal = 6) float lightLevel) {

        Vec3 camPos = GlobalContext.camPos.getCenter();
        FogType fogtype = camera.getFluidInCamera();

        Vec3 targetColor = null;

        ResourceLocation biomeRL = ColorMapUtils.getAccurateBiomeID(level, BlockPos.containing(camPos));

        if (fogtype == FogType.WATER) {
            BiomeWaterFogColorLoader loader = LoaderManager.getLoader(BiomeWaterFogColorLoader.BIOME_WATER_FOG_COLOR_LOADER, BiomeWaterFogColorLoader.class);
            if (loader != null) {
                int colorInt = loader.getColorMapByString(biomeRL.toString());
                Vector4f rgba = ColorMapUtils.int2Vector4fColor(colorInt);
                targetColor = new Vec3(rgba.x, rgba.y, rgba.z);
            }
        } else {
            BiomeFogColorLoader loader = LoaderManager.getLoader(BiomeFogColorLoader.BIOME_FOG_COLOR_LOADER, BiomeFogColorLoader.class);
            if (loader != null) {
                LoaderConfig loaderConfig = LoaderConfig.builder()
                        .rain(level.getRainLevel(Minecraft.getInstance().getFrameTime())) // 使用插值雨量
                        .skyLight((int) lightLevel) // 使用传入的光照等级
                        .camPos(camPos)
                        .renderDistance(renderDistanceChunks)
                        .build();

                Vector4f rgba = loader.findColorByKey(biomeRL.toString(), loaderConfig);
                if (rgba != null) {
                    targetColor = new Vec3(rgba.x, rgba.y, rgba.z);
                }
            }
        }

        if (targetColor == null) {
            targetColor = original.call(center, fetcher);
        }

        if (weather$currentFogColor == null) {
            weather$currentFogColor = targetColor;
        } else {
            double factor = 0.02;
            weather$currentFogColor = weather$currentFogColor.lerp(targetColor, factor);
        }

        return weather$currentFogColor;
    }

}