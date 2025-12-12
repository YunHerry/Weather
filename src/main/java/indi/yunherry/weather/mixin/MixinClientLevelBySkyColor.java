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

        // 如果 Loader 没准备好，或者没有加载数据，回退到原版逻辑
        if (loader == null) {
            return original.call(center, originalFetcher);
        }

        ClientLevel level = (ClientLevel) (Object) this;
        Minecraft mc = Minecraft.getInstance();

        // 2. 计算“目标颜色” (当前位置应该是什么颜色)
        // 我们不需要 CubicSampler 采样周围了，直接取摄像机位置这一个点即可，极大地节省性能
        LoaderConfig config = LoaderConfig.builder()
                .rain(level.getRainLevel(mc.getFrameTime()))
                .skyLight(15)
                .camPos(center)
                .renderDistance(mc.options.renderDistance().get())
                .build();

        // 获取当前位置的 Biome ID
        ResourceLocation biomeRL = ColorMapUtils.getAccurateBiomeID(level, net.minecraft.core.BlockPos.containing(center));

        // 从 Loader 获取目标颜色
        Vector4f targetColorVec4 = loader.findColorByKey(biomeRL.toString(), config);

        // 默认颜色 (如果 Loader 没找到配置)
        Vec3 targetColor;
        if (targetColorVec4 != null) {
            targetColor = new Vec3(targetColorVec4.x, targetColorVec4.y, targetColorVec4.z);
        } else {
            // 如果没找到配置，可以用原版逻辑获取一个基准色作为目标
            targetColor = original.call(center, originalFetcher);
        }

        // 3. 执行“缓动” (时间性平滑)
        if (weather$currentDisplayColor == null) {
            // 初始化：如果是第一次运行（刚进游戏），直接设置为目标色，避免闪烁
            weather$currentDisplayColor = targetColor;
        } else {
            // 核心算法：每一帧让当前颜色向目标颜色移动一小步
            // factor 是平滑系数：
            // 0.05 约等于 1-2 秒完全过渡 (比较慢，很柔和)
            // 0.1 约等于 0.5 秒过渡 (较快)
            // 您可以根据喜好调整这个数值
            double factor = 0.05;

            // 使用 lerp (线性插值) 进行平滑
            weather$currentDisplayColor = weather$currentDisplayColor.lerp(targetColor, factor);
        }

        return weather$currentDisplayColor;
    }
}