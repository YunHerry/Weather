package indi.yunherry.weather.mixin;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import indi.yunherry.weather.WorldContext;
import indi.yunherry.weather.renderer.FogRenderer$Weather;
import indi.yunherry.weather.renderer.ParticleRenderer;
import indi.yunherry.weather.renderer.WeatherRenderer;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeHooksClient;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import javax.annotation.Nullable;

@Mixin(value = FogRenderer.class,priority = 1001)
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

    /**
     * 这个方法同时渲染水下,天空盒,下雨时的情况
     *
     * @author
     * @reason
     */
    //p_234176_代表处于传送门或者药水效果
    @Overwrite
    public static void setupFog(Camera p_234173_, FogRenderer.FogMode p_234174_, float renderDistance, boolean p_234176_, float partialTick) {
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
            //失明
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
                fogrenderer$fogdata.shape = FogShape.CYLINDER;
            }
        } else if (p_234176_) {
            fogrenderer$fogdata.start = renderDistance * 0.05F;
            fogrenderer$fogdata.end = Math.min(renderDistance, 192.0F) * 0.5F;
        } else if (p_234174_ == FogRenderer.FogMode.FOG_SKY) {
            fogrenderer$fogdata.start = 0.0F;
            fogrenderer$fogdata.end = renderDistance;
            fogrenderer$fogdata.shape = FogShape.CYLINDER;
        } else {
            float f = Mth.clamp(renderDistance / 10.0F, 4.0F, 64.0F);
            fogrenderer$fogdata.start = renderDistance - f;
            fogrenderer$fogdata.end = renderDistance;
            fogrenderer$fogdata.shape = FogShape.CYLINDER;
        }

        ParticleRenderer renderer = WorldContext.beans.get("FogRenderer$Weather");
        Minecraft mc = Minecraft.getInstance();
        //TODO: 最后渲染距离应该是从远处慢慢变近这个值不会太低最小值肯定不为0
        if (mc.level != null && p_234174_ != FogRenderer.FogMode.FOG_SKY) {
            if (renderer instanceof FogRenderer$Weather fogRenderer && fogRenderer.isShouldRunning()) {
                float f = Mth.clamp(renderDistance / 10.0F, 4.0F, 64.0F);
                fogrenderer$fogdata.start = renderDistance - f - f * fogRenderer.getPartialTick() * 10;
                fogrenderer$fogdata.end = renderDistance;
//                System.out.println("起始位置: " + fogrenderer$fogdata.start);
//                System.out.println("结束位置: " + fogrenderer$fogdata.start);
//                fogrenderer$fogdata.shape = FogShape.CYLINDER;
            }

        }
//        System.out.println("起始位置: " + fogrenderer$fogdata.start);
//        System.out.println("结束位置: " + fogrenderer$fogdata.end);
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
     * 渲染雾气颜色
     * TODO: 下雨的时候天空颜色会出现骤变 这个方法渲染的是交界处,不是天空
     *
     * @author
     * @reason
     */
    @Overwrite
    public static void setupColor(Camera p_109019_, float p_109020_, ClientLevel p_109021_, int p_109022_, float p_109023_) {
        FogType fogtype = p_109019_.getFluidInCamera();
        Entity entity = p_109019_.getEntity();
        float f13;
        float f15;
        float f16;
        float f5;
        float f7;
        float f9;
        if (fogtype == FogType.WATER) {
            long i = Util.getMillis();
            int j = ((Biome) p_109021_.getBiome(BlockPos.containing(p_109019_.getPosition())).value()).getWaterFogColor();
            if (biomeChangedTime < 0L) {
                targetBiomeFog = j;
                previousBiomeFog = j;
                biomeChangedTime = i;
            }

            int k = targetBiomeFog >> 16 & 255;
            int l = targetBiomeFog >> 8 & 255;
            int i1 = targetBiomeFog & 255;
            int j1 = previousBiomeFog >> 16 & 255;
            int k1 = previousBiomeFog >> 8 & 255;
            int l1 = previousBiomeFog & 255;
            f13 = Mth.clamp((float) (i - biomeChangedTime) / 5000.0F, 0.0F, 1.0F);
            f15 = Mth.lerp(f13, (float) j1, (float) k);
            f16 = Mth.lerp(f13, (float) k1, (float) l);
            float f3 = Mth.lerp(f13, (float) l1, (float) i1);
            fogRed = f15 / 255.0F;
            fogGreen = f16 / 255.0F;
            fogBlue = f3 / 255.0F;
            if (targetBiomeFog != j) {
                targetBiomeFog = j;
                previousBiomeFog = Mth.floor(f15) << 16 | Mth.floor(f16) << 8 | Mth.floor(f3);
                biomeChangedTime = i;
            }
        } else if (fogtype == FogType.LAVA) {
            fogRed = 0.6F;
            fogGreen = 0.1F;
            fogBlue = 0.0F;
            biomeChangedTime = -1L;
        } else if (fogtype == FogType.POWDER_SNOW) {
            fogRed = 0.623F;
            fogGreen = 0.734F;
            fogBlue = 0.785F;
            biomeChangedTime = -1L;
            RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
        } else {
            f5 = 0.25F + 0.75F * (float) p_109022_ / 32.0F;
            f5 = 1.0F - (float) Math.pow((double) f5, 0.25);
            Vec3 vec3 = p_109021_.getSkyColor(p_109019_.getPosition(), p_109020_);
            f7 = (float) vec3.x;
            f9 = (float) vec3.y;
            float f10 = (float) vec3.z;
            float f11 = Mth.clamp(Mth.cos(p_109021_.getTimeOfDay(p_109020_) * 6.2831855F) * 2.0F + 0.5F, 0.0F, 1.0F);
            BiomeManager biomemanager = p_109021_.getBiomeManager();
            Vec3 vec31 = p_109019_.getPosition().subtract(2.0, 2.0, 2.0).scale(0.25);
            Vec3 vec32 = CubicSampler.gaussianSampleVec3(vec31, (p_109033_, p_109034_, p_109035_) -> {
                return p_109021_.effects().getBrightnessDependentFogColor(Vec3.fromRGB24(((Biome) biomemanager.getNoiseBiomeAtQuart(p_109033_, p_109034_, p_109035_).value()).getFogColor()), f11);
            });
            fogRed = (float) vec32.x();
            fogGreen = (float) vec32.y();
            fogBlue = (float) vec32.z();
            if (p_109022_ >= 4) {
                f13 = Mth.sin(p_109021_.getSunAngle(p_109020_)) > 0.0F ? -1.0F : 1.0F;
                Vector3f vector3f = new Vector3f(f13, 0.0F, 0.0F);
                f16 = p_109019_.getLookVector().dot(vector3f);
                if (f16 < 0.0F) {
                    f16 = 0.0F;
                }

                if (f16 > 0.0F) {
                    float[] afloat = p_109021_.effects().getSunriseColor(p_109021_.getTimeOfDay(p_109020_), p_109020_);
                    if (afloat != null) {
                        f16 *= afloat[3];
                        fogRed = fogRed * (1.0F - f16) + afloat[0] * f16;
                        fogGreen = fogGreen * (1.0F - f16) + afloat[1] * f16;
                        fogBlue = fogBlue * (1.0F - f16) + afloat[2] * f16;
                    }
                }
            }

            fogRed += (f7 - fogRed) * f5;
            fogGreen += (f9 - fogGreen) * f5;
            fogBlue += (f10 - fogBlue) * f5;
            f13 = p_109021_.getRainLevel(p_109020_);
            if (f13 > 0.0F) {
                f15 = 1.0F - f13 * 0.5F;
                f16 = 1.0F - f13 * 0.4F;
                fogRed *= f15;
                fogGreen *= f15;
                fogBlue *= f16;
            }

            f15 = p_109021_.getThunderLevel(p_109020_);
            if (f15 > 0.0F) {
                f16 = 1.0F - f15 * 0.5F;
                fogRed *= f16;
                fogGreen *= f16;
                fogBlue *= f16;
            }

            biomeChangedTime = -1L;
        }

        f5 = ((float) p_109019_.getPosition().y - (float) p_109021_.getMinBuildHeight()) * p_109021_.getLevelData().getClearColorScale();
        FogRenderer.MobEffectFogFunction fogrenderer$mobeffectfogfunction = getPriorityFogFunction(entity, p_109020_);
        if (fogrenderer$mobeffectfogfunction != null) {
            LivingEntity livingentity = (LivingEntity) entity;
            f5 = fogrenderer$mobeffectfogfunction.getModifiedVoidDarkness(livingentity, livingentity.getEffect(fogrenderer$mobeffectfogfunction.getMobEffect()), f5, p_109020_);
        }

        if (f5 < 1.0F && fogtype != FogType.LAVA && fogtype != FogType.POWDER_SNOW) {
            if (f5 < 0.0F) {
                f5 = 0.0F;
            }

            f5 *= f5;
            fogRed *= f5;
            fogGreen *= f5;
            fogBlue *= f5;
        }

        if (p_109023_ > 0.0F) {
            fogRed = fogRed * (1.0F - p_109023_) + fogRed * 0.7F * p_109023_;
            fogGreen = fogGreen * (1.0F - p_109023_) + fogGreen * 0.6F * p_109023_;
            fogBlue = fogBlue * (1.0F - p_109023_) + fogBlue * 0.6F * p_109023_;
        }

        if (fogtype == FogType.WATER) {
            if (entity instanceof LocalPlayer) {
                f7 = ((LocalPlayer) entity).getWaterVision();
            } else {
                f7 = 1.0F;
            }
        } else {
            label86:
            {
                if (entity instanceof LivingEntity) {
                    LivingEntity livingentity1 = (LivingEntity) entity;
                    if (livingentity1.hasEffect(MobEffects.NIGHT_VISION) && !livingentity1.hasEffect(MobEffects.DARKNESS)) {
                        f7 = GameRenderer.getNightVisionScale(livingentity1, p_109020_);
                        break label86;
                    }
                }

                f7 = 0.0F;
            }
        }

        if (fogRed != 0.0F && fogGreen != 0.0F && fogBlue != 0.0F) {
            f9 = Math.min(1.0F / fogRed, Math.min(1.0F / fogGreen, 1.0F / fogBlue));
            fogRed = fogRed * (1.0F - f7) + fogRed * f9 * f7;
            fogGreen = fogGreen * (1.0F - f7) + fogGreen * f9 * f7;
            fogBlue = fogBlue * (1.0F - f7) + fogBlue * f9 * f7;
        }

        Vector3f fogColor = ForgeHooksClient.getFogColor(p_109019_, p_109020_, p_109021_, p_109022_, p_109023_, fogRed, fogGreen, fogBlue);
        fogRed = fogColor.x();
        fogGreen = fogColor.y();
        fogBlue = fogColor.z();
        RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
    }
}
