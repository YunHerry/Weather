package indi.yunherry.weather.mixin;

import indi.yunherry.weather.GlobalContext;
import indi.yunherry.weather.TickBlockInfo;
import indi.yunherry.weather.WorldContext;
import indi.yunherry.weather.duck.ICustomTick;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.jline.utils.Log;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(BubbleColumnBlock.class)
public abstract class MixinBubbleColumnBlock extends Block implements ICustomTick {

    public MixinBubbleColumnBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Unique
    @Override
    public void weather$tick(ClientLevel level, TickBlockInfo info) {
        if (!info.state().getValue(BubbleColumnBlock.DRAG_DOWN)) return;
        ThreadLocalRandom randomSource = ThreadLocalRandom.current();
        if (randomSource.nextInt(10) == 0) {

            for(int i = 0; i < randomSource.nextInt(1) + 1; ++i) {
                generateMagmaWaterParticles(info.state(), level,info.pos(),randomSource);
            }
        }

    }
    private void generateMagmaWaterParticles(BlockState state, Level level, BlockPos pos, ThreadLocalRandom random) {
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
