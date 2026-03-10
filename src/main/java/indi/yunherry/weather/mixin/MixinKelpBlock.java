package indi.yunherry.weather.mixin;

import indi.yunherry.weather.TickBlockInfo;
import indi.yunherry.weather.duck.ICustomTick;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(KelpBlock.class)
public abstract class MixinKelpBlock extends GrowingPlantHeadBlock implements LiquidBlockContainer, ICustomTick {
    protected MixinKelpBlock(Properties p_53928_, Direction p_53929_, VoxelShape p_53930_, boolean p_53931_, double p_53932_) {
        super(p_53928_, p_53929_, p_53930_, p_53931_, p_53932_);
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

            float minProb = 0.002f;
            float maxProb = 0.012f;
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
