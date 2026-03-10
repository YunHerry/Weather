package indi.yunherry.weather.mixin;

import indi.yunherry.weather.WorldContext;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BubbleColumnUpParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ThreadLocalRandom;
/**
 * 上升的气泡粒子在离开水面的时候产生涟漪
 * */
@Mixin(BubbleColumnUpParticle.class)
public abstract class MixinBubbleColumnUpParticle extends TextureSheetParticle {


    protected MixinBubbleColumnUpParticle(ClientLevel p_108323_, double p_108324_, double p_108325_, double p_108326_) {
        super(p_108323_, p_108324_, p_108325_, p_108326_);
    }
    @ModifyArg(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;containing(DDD)Lnet/minecraft/core/BlockPos;"
            ),
            index = 1
    )
    private double offsetBubbleFluidCheckY(double originalY) {
        return originalY + 0.2D;
    }
    /**
     * 目标：拦截 tick() 方法中调用的 remove()。
     * 因为 tick() 方法体内只有一个 remove() 调用，所以这个注入点非常安全。
     */
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/BubbleColumnUpParticle;remove()V"))
    private void injectRippleOnPop(CallbackInfo ci) {

        BlockPos currentPos = BlockPos.containing(this.x, this.y, this.z);
        BlockPos abovePos = currentPos.above();

        boolean isWater = level.getFluidState(abovePos).is(FluidTags.WATER);
        boolean isWaterlogged = false;

        if (!isWater) {
            BlockState state = level.getBlockState(abovePos);
            if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                isWaterlogged = state.getValue(BlockStateProperties.WATERLOGGED);
            }
        }
        if (!isWater && !isWaterlogged) {

            double rippleY = currentPos.getY() + 0.8D;

            level.addAlwaysVisibleParticle(
                    WorldContext.particleBeans.get("ripple").get(),
                    this.x,
                    rippleY + 0.1D,
                    this.z,
                    0.0, 0.0, 0.0
            );
        }
    }
}
