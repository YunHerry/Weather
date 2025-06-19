package indi.yunherry.weather.mixin;

import indi.yunherry.weather.WorldContext;
import indi.yunherry.weather.client.particle.WaterVaporParticle;
import net.minecraft.client.particle.CampfireSmokeParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static indi.yunherry.weather.WorldContext.random;
//Refactor: 不合理的判断类的方式
@Mixin(Particle.class)
public abstract class MixinBaseAshSmokeParticle {

    @ModifyVariable(method = "move(DDD)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double addWindDx(double dx){
        if ((Object)this instanceof CampfireSmokeParticle ||(Object)this instanceof WaterVaporParticle) {
            float randomVal = (float) ((random.nextFloat() - 0.5)*0.04);
            return switch (WorldContext.windDirection) {
                case NORTH -> dx + 0.05f + randomVal;
                case SOUTH -> dx - 0.05f + randomVal;
                case NONE -> dx;
                default -> dx;
            };
        }
        return dx;
    }

    @ModifyVariable(method = "move(DDD)V", at = @At("HEAD"), ordinal = 2, argsOnly = true)
    private double addWindDz(double dz){
        if ((Object)this instanceof CampfireSmokeParticle ||(Object)this instanceof WaterVaporParticle) {
            float randomVal = (float) ((random.nextFloat() - 0.5)*0.04);
            return switch (WorldContext.windDirection) {
                case EAST -> dz + 0.05f + randomVal;
                case WEST -> dz - 0.05f + randomVal;
                case NONE -> dz;
                default -> dz;
            };
        }
        return dz;
    }
}
