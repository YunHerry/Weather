package indi.yunherry.weather.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import indi.yunherry.weather.GlobalContext;
import indi.yunherry.weather.event.ColorMapManager;
import indi.yunherry.weather.event.ColorMapResourceLocationConstant;
import indi.yunherry.weather.loader.BiomeFogColorLoader;
import indi.yunherry.weather.loader.BiomeSkyColorLoader;
import indi.yunherry.weather.loader.LoaderConfig;
import indi.yunherry.weather.loader.LoaderManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public abstract class MixinClientLevelBySkyColor extends Level {
    protected MixinClientLevelBySkyColor(WritableLevelData p_270739_, ResourceKey<Level> p_270683_, RegistryAccess p_270200_, Holder<DimensionType> p_270240_, Supplier<ProfilerFiller> p_270692_, boolean p_270904_, boolean p_270470_, long p_270248_, int p_270466_) {
        super(p_270739_, p_270683_, p_270200_, p_270240_, p_270692_, p_270904_, p_270470_, p_270248_, p_270466_);
    }

    @WrapOperation(method = "getSkyColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/CubicSampler;gaussianSampleVec3(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/util/CubicSampler$Vec3Fetcher;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 modifySkyColor$weather(Vec3 center, CubicSampler.Vec3Fetcher fetcher, Operation<Vec3> original) {
        ClientLevel level = ((ClientLevel) (Object) this);
        Vec3 modified = CubicSampler.gaussianSampleVec3(GlobalContext.camPos.getCenter(), (x, y, z) -> {
            BlockPos sample = BlockPos.containing(x, y, z);
            LoaderConfig loaderConfig = LoaderConfig.builder().rain(level.getRainLevel(0)).build();
            ResourceLocation biomeRL = ColorMapManager.getAccurateBiomeID(level, sample);
//            Vector4f rgba = ColorMapManager.getColorReverse(ColorMapResourceLocationConstant.SKY, ColorMapManager.getBiomeIndex(ColorMapResourceLocationConstant.SKY, biomeRL), (float) yAxis);
            BiomeSkyColorLoader loader = LoaderManager.getLoader(BiomeSkyColorLoader.BIOME_SKY_COLOR_LOADER, BiomeSkyColorLoader.class);
            if (loader != null) {
                Vector4f rgba = loader.findColorByKey(biomeRL.toString(), loaderConfig);
                return new Vec3(rgba.x, rgba.y, rgba.z);
            }
            return new Vec3(0.141f, 0.141f, 0.141f); // 默认 #242424
        });
        if (modified != null) return modified;
        return original.call(center, fetcher);
    }
}
