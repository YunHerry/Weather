package indi.yunherry.weather.mixin;

import indi.yunherry.weather.TickBlockInfo;
import indi.yunherry.weather.WorldContext;
import indi.yunherry.weather.duck.ICustomTick;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(TallSeagrassBlock.class)
public abstract class MixinSeagrass extends DoublePlantBlock implements LiquidBlockContainer, ICustomTick {

    public MixinSeagrass(Properties p_154745_) {
        super(p_154745_);
    }

    @Unique
    @Override
    public void weather$tick(ClientLevel level, TickBlockInfo info) {
        ThreadLocalRandom randomSource = ThreadLocalRandom.current();
        BlockPos pos = info.pos();
        if (level.getBlockState(pos.above()).isAir()) {
            if (randomSource.nextFloat() < 0.01) {
                level.addAlwaysVisibleParticle(WorldContext.particleBeans.get("ripple").get(), pos.getX() + 0.5 + ThreadLocalRandom.current().nextFloat(), pos.getY() + 1, pos.getZ() + 0.5 + ThreadLocalRandom.current().nextFloat(), 0.0, 0.0, 0.0);
            }
        } else {
            if (randomSource.nextFloat() < 0.001) {
                level.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, pos.getX() + 0.5 + ThreadLocalRandom.current().nextFloat(), pos.getY() + 1, pos.getZ() + 0.5 + ThreadLocalRandom.current().nextFloat(), 0.0, 0.0, 0.0);
            }
        }

    }
}
