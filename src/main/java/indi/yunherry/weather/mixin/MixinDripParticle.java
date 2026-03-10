package indi.yunherry.weather.mixin;

import indi.yunherry.weather.WorldContext;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
/**
 *
 * 滴水石锥水滴落地
 *
 * */
@Mixin(DripParticle.class)
public abstract class MixinDripParticle extends TextureSheetParticle {

    protected MixinDripParticle(ClientLevel p_108323_, double p_108324_, double p_108325_, double p_108326_) {
        super(p_108323_, p_108324_, p_108325_, p_108326_);
    }
    @Unique
    @Override
    public void remove() {
        super.remove();

        BlockPos pos = BlockPos.containing(this.x, this.y, this.z);
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.WATER)) {

            level.addParticle(
                    WorldContext.particleBeans.get("ripple").get(),
                    this.x, this.y + 0.4, this.z,
                    0.0, 0.0, 0.0
            );

        } else if (state.is(Blocks.WATER_CAULDRON)) {
            double yOffset = switch (state.getValue(LayeredCauldronBlock.LEVEL)) {
                case 1 -> pos.getY() + 0.3125;
                case 2 -> pos.getY() + 0.5;
                case 3 -> pos.getY() + 0.6875;
                default -> pos.getY();
            };
            yOffset += 0.26;
            level.addAlwaysVisibleParticle(
                    WorldContext.particleBeans.get("ripple").get(),
                    this.x, yOffset, this.z,
                    0.0, 0.0, 0.0
            );

        } else if (state.is(Blocks.CAULDRON)) {
            level.addParticle(
                    ParticleTypes.RAIN,
                    this.x, this.y + 1, this.z,
                    0.0, 0.0, 0.0
            );
        }
    }
}
