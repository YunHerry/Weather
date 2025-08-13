package indi.yunherry.weather.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;

@Mixin(value = LevelRenderer.class, priority = 1001)
public class MixinLevelRendererByClouds {

    @Shadow
    @Nullable
    private CloudStatus prevCloudsType;
    @Unique
    private static Vec3 weather$skyColor = new Vec3(0, 0, 0);

    @Redirect(method = "renderClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FogRenderer;levelFogColor()V"))
    private void redirectFogRenderer() {
        RenderSystem.setShaderFogColor((float) weather$skyColor.x, (float) weather$skyColor.y, (float) weather$skyColor.z, 0);
    }

    @Redirect(method = "renderClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getCloudColor(F)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 redirectGetCloudColor(ClientLevel instance, float f7) {
        return instance.getCloudColor(f7);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private BufferBuilder.RenderedBuffer buildClouds(BufferBuilder p_234262_, double p_234263_, double p_234264_, double p_234265_, Vec3 p_234266_) {
        float alpha = 0.5F;
        float f3 = (float) Mth.floor(p_234263_) * 0.00390625F;
        float f4 = (float) Mth.floor(p_234265_) * 0.00390625F;
        float f5 = (float) p_234266_.x;
        float f6 = (float) p_234266_.y;
        float f7 = (float) p_234266_.z;
        float f8 = f5 * 0.9F;
        float f9 = f6 * 0.9F;
        float f10 = f7 * 0.9F;
        float f11 = f5 * 0.7F;
        float f12 = f6 * 0.7F;
        float f13 = f7 * 0.7F;
        float f14 = f5 * 0.8F;
        float f15 = f6 * 0.8F;
        float f16 = f7 * 0.8F;
        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        p_234262_.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        float f17 = (float) Math.floor(p_234264_ / 4.0D) * 4.0F;
        if (this.prevCloudsType == CloudStatus.FANCY) {
            for (int k = -3; k <= 4; ++k) {
                for (int l = -3; l <= 4; ++l) {
                    float f18 = (float) (k * 8);
                    float f19 = (float) (l * 8);
                    if (f17 > -5.0F) {
                        p_234262_.vertex((f18 + 0.0F), (f17 + 0.0F), (f19 + 8.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f11, f12, f13, alpha).normal(0.0F, -1.0F, 0.0F).endVertex();
                        p_234262_.vertex((f18 + 8.0F), (f17 + 0.0F), (f19 + 8.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f11, f12, f13, alpha).normal(0.0F, -1.0F, 0.0F).endVertex();
                        p_234262_.vertex((f18 + 8.0F), (f17 + 0.0F), (f19 + 0.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f11, f12, f13, alpha).normal(0.0F, -1.0F, 0.0F).endVertex();
                        p_234262_.vertex((f18 + 0.0F), (f17 + 0.0F), (f19 + 0.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f11, f12, f13, alpha).normal(0.0F, -1.0F, 0.0F).endVertex();
                    }

                    if (f17 <= 5.0F) {
                        p_234262_.vertex((f18 + 0.0F), (f17 + 4.0F - 9.765625E-4F), (f19 + 8.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f5, f6, f7, alpha).normal(0.0F, 1.0F, 0.0F).endVertex();
                        p_234262_.vertex((f18 + 8.0F), (f17 + 4.0F - 9.765625E-4F), (f19 + 8.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f5, f6, f7, alpha).normal(0.0F, 1.0F, 0.0F).endVertex();
                        p_234262_.vertex((f18 + 8.0F), (f17 + 4.0F - 9.765625E-4F), (f19 + 0.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f5, f6, f7, alpha).normal(0.0F, 1.0F, 0.0F).endVertex();
                        p_234262_.vertex((f18 + 0.0F), (f17 + 4.0F - 9.765625E-4F), (f19 + 0.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f5, f6, f7, alpha).normal(0.0F, 1.0F, 0.0F).endVertex();
                    }

                    if (k > -1) {
                        for (int i1 = 0; i1 < 8; ++i1) {
                            p_234262_.vertex((f18 + (float) i1 + 0.0F), (f17 + 0.0F), (f19 + 8.0F)).uv((f18 + (float) i1 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f8, f9, f10, alpha).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            p_234262_.vertex((f18 + (float) i1 + 0.0F), (f17 + 4.0F), (f19 + 8.0F)).uv((f18 + (float) i1 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f8, f9, f10, alpha).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            p_234262_.vertex((f18 + (float) i1 + 0.0F), (f17 + 4.0F), (f19 + 0.0F)).uv((f18 + (float) i1 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f8, f9, f10, alpha).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            p_234262_.vertex((f18 + (float) i1 + 0.0F), (f17 + 0.0F), (f19 + 0.0F)).uv((f18 + (float) i1 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f8, f9, f10, alpha).normal(-1.0F, 0.0F, 0.0F).endVertex();
                        }
                    }

                    if (k <= 1) {
                        for (int j2 = 0; j2 < 8; ++j2) {
                            p_234262_.vertex((f18 + (float) j2 + 1.0F - 9.765625E-4F), (f17 + 0.0F), (f19 + 8.0F)).uv((f18 + (float) j2 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f8, f9, f10, alpha).normal(1.0F, 0.0F, 0.0F).endVertex();
                            p_234262_.vertex((f18 + (float) j2 + 1.0F - 9.765625E-4F), (f17 + 4.0F), (f19 + 8.0F)).uv((f18 + (float) j2 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f8, f9, f10, alpha).normal(1.0F, 0.0F, 0.0F).endVertex();
                            p_234262_.vertex((f18 + (float) j2 + 1.0F - 9.765625E-4F), (f17 + 4.0F), (f19 + 0.0F)).uv((f18 + (float) j2 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f8, f9, f10, alpha).normal(1.0F, 0.0F, 0.0F).endVertex();
                            p_234262_.vertex((f18 + (float) j2 + 1.0F - 9.765625E-4F), (f17 + 0.0F), (f19 + 0.0F)).uv((f18 + (float) j2 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f8, f9, f10, alpha).normal(1.0F, 0.0F, 0.0F).endVertex();
                        }
                    }

                    if (l > -1) {
                        for (int k2 = 0; k2 < 8; ++k2) {
                            p_234262_.vertex((f18 + 0.0F), (f17 + 4.0F), (f19 + (float) k2 + 0.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float) k2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, alpha).normal(0.0F, 0.0F, -1.0F).endVertex();
                            p_234262_.vertex((f18 + 8.0F), (f17 + 4.0F), (f19 + (float) k2 + 0.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float) k2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, alpha).normal(0.0F, 0.0F, -1.0F).endVertex();
                            p_234262_.vertex((f18 + 8.0F), (f17 + 0.0F), (f19 + (float) k2 + 0.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float) k2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, alpha).normal(0.0F, 0.0F, -1.0F).endVertex();
                            p_234262_.vertex((f18 + 0.0F), (f17 + 0.0F), (f19 + (float) k2 + 0.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float) k2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, alpha).normal(0.0F, 0.0F, -1.0F).endVertex();
                        }
                    }

                    if (l <= 1) {
                        for (int l2 = 0; l2 < 8; ++l2) {
                            p_234262_.vertex((f18 + 0.0F), (f17 + 4.0F), (f19 + (float) l2 + 1.0F - 9.765625E-4F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float) l2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, alpha).normal(0.0F, 0.0F, 1.0F).endVertex();
                            p_234262_.vertex((f18 + 8.0F), (f17 + 4.0F), (f19 + (float) l2 + 1.0F - 9.765625E-4F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float) l2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, alpha).normal(0.0F, 0.0F, 1.0F).endVertex();
                            p_234262_.vertex((f18 + 8.0F), (f17 + 0.0F), (f19 + (float) l2 + 1.0F - 9.765625E-4F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float) l2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, alpha).normal(0.0F, 0.0F, 1.0F).endVertex();
                            p_234262_.vertex((f18 + 0.0F), (f17 + 0.0F), (f19 + (float) l2 + 1.0F - 9.765625E-4F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float) l2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, alpha).normal(0.0F, 0.0F, 1.0F).endVertex();
                        }
                    }
                }
            }
        } else {
            for (int l1 = -32; l1 < 32; l1 += 32) {
                for (int i2 = -32; i2 < 32; i2 += 32) {
                    p_234262_.vertex((l1 + 0), f17, (i2 + 32)).uv((float) (l1 + 0) * 0.00390625F + f3, (float) (i2 + 32) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                    p_234262_.vertex((l1 + 32), f17, (i2 + 32)).uv((float) (l1 + 32) * 0.00390625F + f3, (float) (i2 + 32) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                    p_234262_.vertex((l1 + 32), f17, (i2 + 0)).uv((float) (l1 + 32) * 0.00390625F + f3, (float) (i2 + 0) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                    p_234262_.vertex((l1 + 0), f17, (i2 + 0)).uv((float) (l1 + 0) * 0.00390625F + f3, (float) (i2 + 0) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                }
            }
        }
        return p_234262_.end();
    }

}
