package indi.yunherry.weather.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import indi.yunherry.weather.loader.BiomeSkyColorLoader;
import indi.yunherry.weather.loader.LoaderConfig;
import indi.yunherry.weather.loader.LoaderManager;
import indi.yunherry.weather.utils.ColorMapUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CubicSampler;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientLevel.class)
public abstract class MixinClientLevelBySkyColor {

    // 1. 保存“当前显示的颜色” (上一帧的颜色)
    @Unique
    private Vec3 weather$currentDisplayColor;

    @WrapOperation(
            method = "getSkyColor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/CubicSampler;gaussianSampleVec3(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/util/CubicSampler$Vec3Fetcher;)Lnet/minecraft/world/phys/Vec3;")
    )
    private Vec3 modifySkyColor$weather(Vec3 center, CubicSampler.Vec3Fetcher originalFetcher, Operation<Vec3> original) {
        // 获取 Loader
        BiomeSkyColorLoader loader = LoaderManager.getLoader(BiomeSkyColorLoader.BIOME_SKY_COLOR_LOADER, BiomeSkyColorLoader.class);

        if (loader == null) {
            return original.call(center, originalFetcher);
        }

        ClientLevel level = (ClientLevel) (Object) this;
        Minecraft mc = Minecraft.getInstance();

        LoaderConfig config = LoaderConfig.builder()
                .rain(level.getRainLevel(mc.getFrameTime()))
                .skyLight(15)
                .camPos(center)
                .renderDistance(mc.options.renderDistance().get())
                .build();

        ResourceLocation biomeRL = ColorMapUtils.getAccurateBiomeID(level, net.minecraft.core.BlockPos.containing(center));

        Vector4f targetColorVec4 = loader.findColorByKey(biomeRL.toString(), config);

        Vec3 targetColor;
        if (targetColorVec4 != null) {
            targetColor = new Vec3(targetColorVec4.x, targetColorVec4.y, targetColorVec4.z);
        } else {
            targetColor = original.call(center, originalFetcher);
        }

        if (weather$currentDisplayColor == null) {
            weather$currentDisplayColor = targetColor;
        } else {
            double factor = 0.05;
            weather$currentDisplayColor = weather$currentDisplayColor.lerp(targetColor, factor);
        }

        return weather$currentDisplayColor;
    }
}