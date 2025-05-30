package indi.yunherry.weather.mixin;

import indi.yunherry.weather.ParticleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.SeagrassBlock;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(TallSeagrassBlock.class)
public abstract class MixinSeagrass extends DoublePlantBlock implements LiquidBlockContainer {

    public MixinSeagrass(Properties p_154745_) {
        super(p_154745_);
    }
    @Unique
    @Override
    public void animateTick(BlockState p_220827_, Level p_220828_, BlockPos p_220829_, RandomSource p_220830_) {
        super.animateTick(p_220827_, p_220828_, p_220829_, p_220830_);
        if (p_220830_.nextFloat()<0.05) {
            p_220828_.addParticle(ParticleRegistry.RIPPLE.get(), p_220829_.getX()+0.5+ ThreadLocalRandom.current().nextFloat(), p_220829_.getY() + 1, p_220829_.getZ()+0.5+ ThreadLocalRandom.current().nextFloat(), 0.0, 0.0, 0.0);

        }

    }
}
