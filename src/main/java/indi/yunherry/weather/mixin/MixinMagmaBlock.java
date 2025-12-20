package indi.yunherry.weather.mixin;

import indi.yunherry.weather.WorldContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MagmaBlock.class)
public abstract class MixinMagmaBlock extends Block {

    public MixinMagmaBlock(Properties p_49795_) {
        super(p_49795_);
    }

//    @Override
//    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
//        super.animateTick(state, level, pos, random);
//        generateMagmaWaterParticles(state, level, pos, random);
//    }

//    @Override
//    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
//        super.randomTick(state, level, pos, random);
//        if (level.getBlockState(pos.above()).getFluidState().is(Fluids.WATER)) {
//            generateMagmaWaterParticles(state, level, pos, random);
//        }
//    }



    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                net.minecraft.world.level.block.Block block,
                                BlockPos fromPos, boolean isMoving) {
        //更新的时候检测
    }
}