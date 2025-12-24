package indi.yunherry.weather.mixin;

import indi.yunherry.weather.CustomBlockEntityThreadPool;
import indi.yunherry.weather.duck.ICustomTick;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.class)
public class MixinBlockBehaviour {

    @Inject(method = "onPlace", at = @At("RETURN"))
    private void onPlace(BlockState p_60566_, Level p_60567_, BlockPos p_60568_, BlockState p_60569_, boolean p_60570_, CallbackInfo ci) {

        if (!p_60566_.is(p_60569_.getBlock())) {
            if (p_60566_.is(Blocks.BUBBLE_COLUMN)) {
                if (p_60566_.getBlock() instanceof ICustomTick customTick && p_60567_.getBlockState(p_60568_.above()).isAir()) {
                    CustomBlockEntityThreadPool.submitTicker(customTick, p_60568_.immutable(), p_60566_);
                }
            } else if (p_60566_.is(Blocks.TALL_SEAGRASS)) {
                if (p_60566_.getBlock() instanceof ICustomTick customTick) {
                    CustomBlockEntityThreadPool.submitTicker(customTick, p_60568_.immutable(), p_60566_);
                }
            }
        }
    }

    @Inject(method = "onRemove", at = @At("RETURN"))
    private void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving, CallbackInfo ci) {

        if (!state.is(newState.getBlock())) {
            CustomBlockEntityThreadPool.removeTickerAt(pos.immutable());
        }
    }
}
