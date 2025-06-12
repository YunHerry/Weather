package indi.yunherry.weather.mixin;

import indi.yunherry.weather.WorldContext;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DripParticle.class)
public abstract class MixinDripParticle extends TextureSheetParticle {

    protected MixinDripParticle(ClientLevel p_108323_, double p_108324_, double p_108325_, double p_108326_) {
        super(p_108323_, p_108324_, p_108325_, p_108326_);
    }
    @Unique
    @Override
    public void remove() {
        super.remove();
        //奇怪的判定
        BlockPos pos = new BlockPos((int) this.x, (int) this.y, (int) this.z).west();
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() == Blocks.WATER) {
            level.addParticle(WorldContext.particleBeans.get("ripple").get(), this.x, this.y+0.4, this.z, 0.0, 0.0, 0.0);
        } else if (state.getBlock() == Blocks.WATER_CAULDRON) {
            double length = switch (state.getValue(LayeredCauldronBlock.LEVEL)) {
                case 1 -> this.y + 0.4;
                case 2 -> this.y + 0.6;
                case 3 -> this.y + 0.7;
                default -> 0;
            };
            level.addParticle(WorldContext.particleBeans.get("ripple").get(), this.x,  length, this.z, 0.0, 0.0, 0.0);
        } else if (state.getBlock() == Blocks.CAULDRON) {
            level.addParticle(ParticleTypes.RAIN,this.x, this.y+1, this.z, 0.0, 0.0, 0.0);
        }
    }
}
