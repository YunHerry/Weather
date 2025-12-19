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
import org.spongepowered.asm.mixin.injection.ModifyArg;
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

//    @ModifyArg(
//            method = "buildClouds",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;color(FFFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
//            ),
//            index = 3
//    )
//    private float modifyCloudAlpha(float originalAlpha) {
//        float multiplier = 0.5F;
//        return originalAlpha * multiplier;
//    }

}
