package indi.yunherry.weather.mixin;

import indi.yunherry.weather.WorldContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(MagmaBlock.class)
public abstract class MixinMagmaBlock extends Block {

    public MixinMagmaBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public void animateTick(BlockState p_220827_, Level p_220828_, BlockPos p_220829_, RandomSource randomSource) {
        if(p_220828_.getBlockState(p_220829_.above()).getFluidState().is(Fluids.WATER)) {
            p_220828_.addAlwaysVisibleParticle(WorldContext.particleBeans.get("water_vapor").get(),true,(double)p_220829_.getX() + 0.5D + randomSource.nextDouble() / 3.0D * (double)(randomSource.nextBoolean() ? 1 : -1), (double)p_220829_.getY() + randomSource.nextDouble() + randomSource.nextDouble(), (double)p_220829_.getZ() + 0.5D + randomSource.nextDouble() / 3.0D * (double)(randomSource.nextBoolean() ? 1 : -1), 0.0D, 0.07D, 0.0D);
        }
    }
}
