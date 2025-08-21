package indi.yunherry.weather.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import indi.yunherry.weather.GlobalContext;
import indi.yunherry.weather.loader.*;
import indi.yunherry.weather.utils.ColorMapUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeHooksClient;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;

import static com.mojang.blaze3d.shaders.FogShape.CYLINDER;

@Mixin(value = FogRenderer.class, priority = 1001)
public abstract class MixinFogRenderer {


    /**
     * @author
     * @reason
     */
    @Overwrite
    public static void setupFog(Camera p_234173_, FogRenderer.FogMode p_234174_, float p_234175_, boolean p_234176_, float p_234177_) {
        FogType fogtype = p_234173_.getFluidInCamera();
        Entity entity = p_234173_.getEntity();
        FogRenderer.FogData fogrenderer$fogdata = new FogRenderer.FogData(p_234174_);
        FogRenderer.MobEffectFogFunction fogrenderer$mobeffectfogfunction = getPriorityFogFunction(entity, p_234177_);
        if (fogtype == FogType.LAVA) {
            if (entity.isSpectator()) {
                fogrenderer$fogdata.start = -8.0F;
                fogrenderer$fogdata.end = p_234175_ * 0.5F;
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
                fogrenderer$fogdata.end = p_234175_ * 0.5F;
            } else {
                fogrenderer$fogdata.start = 0.0F;
                fogrenderer$fogdata.end = 2.0F;
            }
        } else if (fogrenderer$mobeffectfogfunction != null) {
            LivingEntity livingentity = (LivingEntity) entity;
            MobEffectInstance mobeffectinstance = livingentity.getEffect(fogrenderer$mobeffectfogfunction.getMobEffect());
            if (mobeffectinstance != null) {
                fogrenderer$mobeffectfogfunction.setupFog(fogrenderer$fogdata, livingentity, mobeffectinstance, p_234175_, p_234177_);
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

            if (fogrenderer$fogdata.end > p_234175_) {
                fogrenderer$fogdata.end = p_234175_;
                fogrenderer$fogdata.shape = CYLINDER;
            }
        } else if (p_234176_) {
            fogrenderer$fogdata.start = p_234175_ * 0.05F;
            fogrenderer$fogdata.end = Math.min(p_234175_, 192.0F) * 0.5F;
        } else if (p_234174_ == FogRenderer.FogMode.FOG_SKY) {
            fogrenderer$fogdata.start = 0.0F;
            fogrenderer$fogdata.end = p_234175_;
            fogrenderer$fogdata.shape = CYLINDER;
        } else {
            float f = Mth.clamp(p_234175_ / 10.0F, 4.0F, 64.0F);
            fogrenderer$fogdata.start = p_234175_ - f;
            fogrenderer$fogdata.end = p_234175_;
            fogrenderer$fogdata.shape = CYLINDER;

        }

        if ( p_234174_ == FogRenderer.FogMode.FOG_TERRAIN) {
            BiomeFogDistanceLoader.FogState targetFog = BiomeFogDistanceLoader.modifyBiomeFog(fogrenderer$fogdata.start, fogrenderer$fogdata.end);
            if (targetFog != null) {
                fogrenderer$fogdata.start = targetFog.start();
                fogrenderer$fogdata.end = targetFog.end();
            }
        }

        RenderSystem.setShaderFogStart(fogrenderer$fogdata.start);
        RenderSystem.setShaderFogEnd(fogrenderer$fogdata.end);
        RenderSystem.setShaderFogShape(fogrenderer$fogdata.shape);

        ForgeHooksClient.onFogRender(p_234174_, fogtype, p_234173_, p_234177_, p_234175_, fogrenderer$fogdata.start, fogrenderer$fogdata.end, fogrenderer$fogdata.shape);
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
        Vec3 modified = level.effects().getBrightnessDependentFogColor(CubicSampler.gaussianSampleVec3(camPos, (x, y, z) -> {
            BlockPos sample = BlockPos.containing(x, y, z);
            Vector4f rgba;

            ResourceLocation biomeRL = ColorMapUtils.getAccurateBiomeID(level, sample);
            if (fogtype == FogType.WATER) {
                BiomeWaterFogColorLoader loader = LoaderManager.getLoader(BiomeWaterFogColorLoader.BIOME_WATER_FOG_COLOR_LOADER, BiomeWaterFogColorLoader.class);
                rgba = ColorMapUtils.int2Vector4fColor(loader.getColorMapByString(biomeRL.toString()));
            } else {
                LoaderConfig loaderConfig = LoaderConfig.builder().rain(level.getRainLevel(0)).skyLight((int) lightLevel).build();
                BiomeFogColorLoader loader = LoaderManager.getLoader(BiomeFogColorLoader.BIOME_FOG_COLOR_LOADER, BiomeFogColorLoader.class);
                rgba = loader.findColorByKey(biomeRL.toString(), loaderConfig);
            }
            return new Vec3(rgba.x, rgba.y, rgba.z);
        }), lightLevel);
        return modified;
    }

    @Shadow
    @Nullable
    private static FogRenderer.MobEffectFogFunction getPriorityFogFunction(Entity p_234166_, float p_234167_) {
        return null;
    }

}