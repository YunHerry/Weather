package indi.yunherry.weather.mixin;

import indi.yunherry.weather.GlobalContext;
import indi.yunherry.weather.loader.*;
import indi.yunherry.weather.utils.ColorMapUtils;
import indi.yunherry.weather.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CubicSampler;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(BiomeColors.class)
public class MixinBiomeColors {
    @Inject(method = "getAverageWaterColor", at = @At("HEAD"), cancellable = true)
    private static void modifyWaterColor(BlockAndTintGetter level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (GlobalContext.level != null) {
            BiomeWaterColorLoader loader = LoaderManager.getLoader(BiomeWaterColorLoader.BIOME_WATER_COLOR_LOADER, BiomeWaterColorLoader.class);

            if (loader != null) {
//                // 获取混合后的颜色而不是单点颜色
//                int blendedColor = getBlendedWaterColor(GlobalContext.level, pos, loader);
//                if (blendedColor != -1) {
//                    cir.setReturnValue(blendedColor);
//                }
                cir.setReturnValue(ColorUtils.vec3Color2Int(CubicSampler.gaussianSampleVec3(pos.getCenter(), (x, y, z) -> {
                    BlockPos sample = BlockPos.containing(x, y, z);
                    ResourceLocation biomeRL = ColorMapUtils.getAccurateBiomeID(GlobalContext.level, sample);

//                    BiomeWaterColorLoader loader = LoaderManager.getLoader(BiomeWaterColorLoader.BIOME_WATER_COLOR_LOADER, BiomeWaterColorLoader.class);
                    Vector4f rgba = ColorMapUtils.int2Vector4fColor(loader.getColorMapByString(biomeRL.toString()));
                    return new Vec3(rgba.x, rgba.y, rgba.z);
                })));
            }
        }
    }
}
