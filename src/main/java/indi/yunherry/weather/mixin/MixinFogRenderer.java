package indi.yunherry.weather.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import indi.yunherry.weather.GlobalContext;
import indi.yunherry.weather.WorldContext;
import indi.yunherry.weather.loader.BiomeFogColorLoader;
import indi.yunherry.weather.loader.LoaderConfig;
import indi.yunherry.weather.loader.LoaderManager;
import indi.yunherry.weather.renderer.FogRenderer$Weather;
import indi.yunherry.weather.renderer.ParticleRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeHooksClient;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;

import static com.mojang.blaze3d.shaders.FogShape.CYLINDER;

@Mixin(value = FogRenderer.class, priority = 1001)
public abstract class MixinFogRenderer {
    @Shadow
    private static long biomeChangedTime;
    @Shadow
    private static int targetBiomeFog;
    @Shadow
    private static int previousBiomeFog;
    @Shadow
    private static float fogRed;
    @Shadow
    private static float fogGreen;
    @Shadow
    private static float fogBlue;

    // 存储当前的雾气模式，用于区分云渲染和地面雾气渲染
    private static FogRenderer.FogMode currentFogMode = null;


    /**
     * @author
     * @reason
     */
    @Overwrite
    public static void setupFog(Camera p_234173_, FogRenderer.FogMode p_234174_, float renderDistance, boolean p_234176_, float partialTick) {
        // 存储当前雾气模式
        currentFogMode = p_234174_;

        FogType fogtype = p_234173_.getFluidInCamera();
        Entity entity = p_234173_.getEntity();
        FogRenderer.FogData fogrenderer$fogdata = new FogRenderer.FogData(p_234174_);
        FogRenderer.MobEffectFogFunction fogrenderer$mobeffectfogfunction = getPriorityFogFunction(entity, partialTick);

        if (fogtype == FogType.LAVA) {
            if (entity.isSpectator()) {
                fogrenderer$fogdata.start = -8.0F;
                fogrenderer$fogdata.end = renderDistance * 0.5F;
            } else if (entity instanceof LivingEntity && ((LivingEntity) entity).hasEffect(MobEffects.FIRE_RESISTANCE)) {
                fogrenderer$fogdata.start = 0.0F;
                fogrenderer$fogdata.end = 3.0F;
            } else {
                fogrenderer$fogdata.start = 0.25F;
                fogrenderer$fogdata.end = 1.0F;
            }
        } else if (fogtype == FogType.POWDER_SNOW) {
            if (entity.isSpectator()) {
                fogrenderer$fogdata.start = -8.0F;
                fogrenderer$fogdata.end = renderDistance * 0.5F;
            } else {
                fogrenderer$fogdata.start = 0.0F;
                fogrenderer$fogdata.end = 2.0F;
            }
        } else if (fogrenderer$mobeffectfogfunction != null) {
            LivingEntity livingentity = (LivingEntity) entity;
            MobEffectInstance mobeffectinstance = livingentity.getEffect(fogrenderer$mobeffectfogfunction.getMobEffect());
            if (mobeffectinstance != null) {
                fogrenderer$mobeffectfogfunction.setupFog(fogrenderer$fogdata, livingentity, mobeffectinstance, renderDistance, partialTick);
            }
        } else if (fogtype == FogType.WATER) {
            fogrenderer$fogdata.start = -8.0F;
            fogrenderer$fogdata.end = 96.0F;
            if (entity instanceof LocalPlayer) {
                LocalPlayer localplayer = (LocalPlayer) entity;
                fogrenderer$fogdata.end *= Math.max(0.25F, localplayer.getWaterVision());
                Holder<Biome> holder = localplayer.level().getBiome(localplayer.blockPosition());
                if (holder.is(BiomeTags.HAS_CLOSER_WATER_FOG)) {
                    fogrenderer$fogdata.end *= 0.85F;
                }
            }

            if (fogrenderer$fogdata.end > renderDistance) {
                fogrenderer$fogdata.end = renderDistance;
                fogrenderer$fogdata.shape = CYLINDER;
            }
        } else if (p_234176_) {
            fogrenderer$fogdata.start = renderDistance * 0.05F;
            fogrenderer$fogdata.end = Math.min(renderDistance, 192.0F) * 0.5F;
        } else if (p_234174_ == FogRenderer.FogMode.FOG_SKY) {
            // 天空雾气模式 - 用于云渲染，保持原版逻辑
            fogrenderer$fogdata.start = 0.0F;
            fogrenderer$fogdata.end = renderDistance;
            fogrenderer$fogdata.shape = CYLINDER;
        } else {
            // 地面雾气模式 - 应用自定义雾气效果
            float f = Mth.clamp(renderDistance / 10.0F, 4.0F, 64.0F);
            fogrenderer$fogdata.start = renderDistance - f;
            fogrenderer$fogdata.end = renderDistance;
            fogrenderer$fogdata.shape = CYLINDER;

            // 只在地面雾气模式下应用自定义天气雾气效果
            ParticleRenderer renderer = WorldContext.beans.get("FogRenderer$Weather");
            Minecraft mc = Minecraft.getInstance();

            if (mc.level != null && renderer instanceof FogRenderer$Weather fogRenderer && fogRenderer.isShouldRunning()) {
                Level level = GlobalContext.level;
                float rain = level.getRainLevel(partialTick);
                float playerY = (float) p_234173_.getPosition().y;
                int skylight = level.getBrightness(LightLayer.SKY, p_234173_.getBlockPosition());

                float fogRadius = 1f
                        - rain * 0.1f
                        + (rain > 0f && playerY < 47f ? rain * 0.1f : 0f)
                        + (playerY < 47f ? 0.15f : 0f)
                        - (playerY <= -54f && skylight == 0 ? 0.75f : 0f);

                float factorA = 1f
                        + (renderDistance / 64f) * 0.5f
                        + rain * 0.4f
                        - (rain > 0f && playerY < 47f ? rain * 0.4f : 0f)
                        - (playerY < 47f ? 0.1f : 0f);

                float factorB = 7.5f + (renderDistance / 64f * 2.5f);
                float fogFade = factorA * factorB;

                fogrenderer$fogdata.start = renderDistance - f - f * fogRenderer.getPartialTick() * 10;
                fogrenderer$fogdata.end = renderDistance + f * fogRenderer.getPartialTick() * 2;
                fogrenderer$fogdata.shape = FogShape.CYLINDER;

//                System.out.println("地面雾气 - 起始位置: " + fogrenderer$fogdata.start + ", 结束位置: " + fogrenderer$fogdata.end);
            }
        }

        RenderSystem.setShaderFogStart(fogrenderer$fogdata.start);
        RenderSystem.setShaderFogEnd(fogrenderer$fogdata.end);
        RenderSystem.setShaderFogShape(fogrenderer$fogdata.shape);
        ForgeHooksClient.onFogRender(p_234174_, fogtype, p_234173_, partialTick, renderDistance, fogrenderer$fogdata.start, fogrenderer$fogdata.end, fogrenderer$fogdata.shape);
    }

    @Shadow
    @Nullable
    protected static FogRenderer.MobEffectFogFunction getPriorityFogFunction(Entity p_234166_, float p_234167_) {
        return null;
    }

    /**
     * 修改雾气颜色采样 - 只在地面雾气模式下应用自定义颜色
     */
    @WrapOperation(
            method = "setupColor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/CubicSampler;gaussianSampleVec3(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/util/CubicSampler$Vec3Fetcher;)Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private static Vec3 modifyFogColor$weather(Vec3 center, CubicSampler.Vec3Fetcher fetcher,
                                               Operation<Vec3> original,
                                               @Local(argsOnly = true) ClientLevel level,
                                               @Local(argsOnly = true) int renderDistanceChunks,
                                               @Local(ordinal = 6) float lightLevel) {

        if (currentFogMode == FogRenderer.FogMode.FOG_SKY) {
            return original.call(center, fetcher);
        }

        Vec3 camPos = GlobalContext.camPos.getCenter();
        Vec3 modified = level.effects().getBrightnessDependentFogColor(CubicSampler.gaussianSampleVec3(camPos, (x, y, z) -> {
            BlockPos sample = BlockPos.containing(x, y, z);

            LoaderConfig loaderConfig = LoaderConfig.builder().rain(level.getRainLevel(0)).skyLight((int) lightLevel).build();
            ResourceLocation biomeRL = getAccurateBiomeID(level, sample);

            BiomeFogColorLoader loader = LoaderManager.getLoader(BiomeFogColorLoader.BIOME_FOG_COLOR_LOADER, BiomeFogColorLoader.class);
            if (loader != null) {
                Vector4f rgba = loader.findColorByKey(biomeRL.toString(),loaderConfig);
                return new Vec3(rgba.x, rgba.y, rgba.z);
            }
            return new Vec3(0.141f, 0.141f, 0.141f); // 默认 #242424
        }), lightLevel);

        if (modified != null) {
//            System.out.println("应用自定义地面雾气颜色: " + modified);
            return modified;
        }
        return original.call(center, fetcher);
    }

    // 辅助方法 - 需要添加到类中
    private static ResourceLocation getAccurateBiomeID(Level level, BlockPos pos) {
        int quartX = QuartPos.fromBlock(pos.getX());
        int quartY = QuartPos.fromBlock(pos.getY());
        int quartZ = QuartPos.fromBlock(pos.getZ());

        LevelChunk chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        Holder<Biome> holder = chunk.getNoiseBiome(quartX, quartY, quartZ);

        Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);
        return biomeRegistry.getResourceKey(holder.value())
                .map(ResourceKey::location)
                .orElse(new ResourceLocation("minecraft", "plains"));
    }


}