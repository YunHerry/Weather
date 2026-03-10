package indi.yunherry.weather.mixin;

import indi.yunherry.weather.TickBlockInfo;
import indi.yunherry.weather.duck.ICustomTick;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.SeagrassBlock;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(TallSeagrassBlock.class)
public abstract class MixinTallSeagrassBlock extends DoublePlantBlock implements LiquidBlockContainer, ICustomTick {
    public MixinTallSeagrassBlock(Properties p_52861_) {
        super(p_52861_);
    }

    @Unique
    @Override
    public void weather$tick(ClientLevel level, TickBlockInfo info) {
        ThreadLocalRandom randomSource = ThreadLocalRandom.current();
        BlockPos pos = info.pos();

        // 1. 获取光照数据
        int rawSkyLight = level.getBrightness(LightLayer.SKY, pos);
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int skyDarken = level.getSkyDarken();
        int actualSkyLight = Math.max(0, rawSkyLight - skyDarken);

        if (level.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER)) {
            int effectiveLight = Math.max(actualSkyLight, blockLight);

            float minProb = 0.001f;
            float maxProb = 0.01f;
            float spawnThreshold = minProb + (maxProb - minProb) * (effectiveLight / 15.0f);

            // 3. 产生向上飘的气泡
            if (randomSource.nextFloat() < spawnThreshold) {
                Vec3 posCenter = info.pos().getCenter();
                double particleX = posCenter.x + randomSource.nextFloat();
                double particleY = posCenter.y + randomSource.nextFloat();
                double particleZ = posCenter.z + randomSource.nextFloat();
                level.addAlwaysVisibleParticle(
                        ParticleTypes.BUBBLE_COLUMN_UP,
                        particleX, particleY, particleZ,
                        0.0D, 0.0D, 0.0D
                );
            }
        }
    }
}
