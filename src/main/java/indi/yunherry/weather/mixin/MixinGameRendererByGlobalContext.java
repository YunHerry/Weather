package indi.yunherry.weather.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import indi.yunherry.weather.GlobalContext;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/**
 *
 * update global context
 * */
@Mixin(GameRenderer.class)
public class MixinGameRendererByGlobalContext {
    @Inject(method = "renderLevel",at= @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;prepareCullFrustum(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;Lorg/joml/Matrix4f;)V"))
    public void weather$renderLevel(float p_109090_, long p_109091_, PoseStack p_109092_, CallbackInfo ci) {
        GlobalContext.update();
    }
}
