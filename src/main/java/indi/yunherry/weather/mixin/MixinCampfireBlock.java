package indi.yunherry.weather.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CampfireBlock.class)
public class MixinCampfireBlock  {
//    @Redirect(method = "makeParticles",at= @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addAlwaysVisibleParticle(Lnet/minecraft/core/particles/ParticleOptions;ZDDDDDD)V"))
//    private static void redirectAddAlwaysVisibleParticle(
//            Level instance, ParticleOptions p_46691_, boolean p_46692_, double p_46693_, double p_46694_, double p_46695_, double p_46696_, double p_46697_, double p_46698_
//    ) {
//        RandomSource randomSource = RandomSource.create();
//        instance.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, true, (double)p_46693_ + 0.5D + randomSource.nextDouble() / 3.0D * (double)(randomSource.nextBoolean() ? 1 : -1), (double)p_46694_ + randomSource.nextDouble() + randomSource.nextDouble(), (double)p_46695_ + 0.5D + randomSource.nextDouble() / 3.0D * (double)(randomSource.nextBoolean() ? 1 : -1), 0.0D, 0.07D, 0.0D);
//    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public static void makeParticles(Level p_51252_, BlockPos p_51253_, boolean p_51254_, boolean p_51255_) {
        RandomSource randomsource = p_51252_.getRandom();
//        SimpleParticleType simpleparticletype = p_51254_ ? ParticleTypes.CAMPFIRE_SIGNAL_SMOKE : ParticleTypes.CAMPFIRE_COSY_SMOKE;
        p_51252_.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, (double) p_51253_.getX() + 0.5D + randomsource.nextDouble() / 3.0D * (double) (randomsource.nextBoolean() ? 1 : -1), (double) p_51253_.getY() + randomsource.nextDouble() + randomsource.nextDouble(), (double) p_51253_.getZ() + 0.5D + randomsource.nextDouble() / 3.0D * (double) (randomsource.nextBoolean() ? 1 : -1), 0.0D, 0.07D, 0.0D);
        if (p_51255_) {
            p_51252_.addParticle(ParticleTypes.SMOKE, (double) p_51253_.getX() + 0.5D + randomsource.nextDouble() / 4.0D * (double) (randomsource.nextBoolean() ? 1 : -1), (double) p_51253_.getY() + 0.4D, (double) p_51253_.getZ() + 0.5D + randomsource.nextDouble() / 4.0D * (double) (randomsource.nextBoolean() ? 1 : -1), 0.0D, 0.005D, 0.0D);
        }

    }
}
