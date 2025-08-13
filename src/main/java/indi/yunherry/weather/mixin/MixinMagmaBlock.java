package indi.yunherry.weather.mixin;

import indi.yunherry.weather.WorldContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        generateMagmaWaterParticles(state, level, pos, random);
    }

    // 添加随机 tick 方法
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (level.getBlockState(pos.above()).getFluidState().is(Fluids.WATER)) {
            generateMagmaWaterParticles(state, level, pos, random);
        }
    }

    private void generateMagmaWaterParticles(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockState(pos.above()).getFluidState().is(Fluids.WATER)) {
            double x = pos.getX() + 0.5D + random.nextDouble() / 3.0D * (random.nextBoolean() ? 1 : -1);
            double y = pos.getY() + random.nextDouble() + random.nextDouble();
            double z = pos.getZ() + 0.5D + random.nextDouble() / 3.0D * (random.nextBoolean() ? 1 : -1);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        WorldContext.particleBeans.get("water_vapor").get(),
                        x, y, z, 1,
                        0.0D, 0.07D, 0.0D, 0.0D
                );
            } else {
                level.addAlwaysVisibleParticle(
                        WorldContext.particleBeans.get("water_vapor").get(),
                        true, x, y, z,
                        0.0D, 0.07D, 0.0D
                );
            }
        }
    }
}