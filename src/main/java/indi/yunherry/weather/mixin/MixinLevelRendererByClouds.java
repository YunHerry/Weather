package indi.yunherry.weather.mixin;

import com.llamalad7.mixinextras.sugar.Local;
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
import org.spongepowered.asm.mixin.injection.*;

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
     * 修改云朵渲染时的透明度
     * target: 指向 VertexConsumer.color(FFFF) 方法
     * index: 3 表示修改第 4 个参数 (alpha)
     */
    @ModifyArg(
            method = "buildClouds",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;color(FFFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
            ),
            index = 3
    )
    private float injectCloudAlpha(float originalAlpha) {
        // 根据你的代码逻辑：只有在 FANCY (高品质) 模式下才修改为 0.5F
        // 如果是 FAST 模式，原版也是 0.8F，你可以根据需要决定是否修改
        if (this.prevCloudsType == CloudStatus.FANCY) {
            return 0.5F; // 你的自定义透明度
        }
        return originalAlpha; // 保持原样 (0.8F)
    }
}
