package indi.yunherry.weather.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Function;

@Mixin(WaterDropParticle.class)
public abstract class MixinWaterDropParticle extends TextureSheetParticle {
    @Unique
    private static final float MAX_LENGTH = 32.0F;
    private static final Logger log = LoggerFactory.getLogger(MixinWaterDropParticle.class);
    @Unique
    private final Function<ClipContext, BlockHitResult> raycaster = level::clip;
    protected MixinWaterDropParticle(ClientLevel p_108328_, double p_108329_, double p_108330_, double p_108331_, double p_108332_, double p_108333_, double p_108334_, Function<ClipContext, BlockHitResult> raycaster) {
        super(p_108328_, p_108329_, p_108330_, p_108331_, p_108332_, p_108333_, p_108334_);
    }

    protected MixinWaterDropParticle(ClientLevel p_108323_, double p_108324_, double p_108325_, double p_108326_, Function<ClipContext, BlockHitResult> raycaster) {
        super(p_108323_, p_108324_, p_108325_, p_108326_);
    }
    @Overwrite
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        Vec3 start = new Vec3(this.x, this.y, this.z);
        Vec3 end = new Vec3(0, -1, 0).scale(MAX_LENGTH).add(start);
        ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, null);
        BlockHitResult result = this.raycaster.apply(context);
        if (this.lifetime-- <= 0) {
            this.remove();
        } else {
            this.yd -= (double) this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.xd *= (double) 0.98F;
            this.yd *= (double) 0.98F;
            this.zd *= (double) 0.98F;
            if (this.onGround) {
             if (Math.random() < 0.5D) {
                 this.remove();
             }

             this.xd *= (double) 0.7F;
             this.zd *= (double) 0.7F;
            }

            BlockPos blockpos = result.getBlockPos();
            double d0 = Math.max(result.getLocation().y, (double) this.level.getFluidState(blockpos).getHeight(this.level, blockpos));
            if (d0 > 0.0D && this.y < (double) blockpos.getY() + d0) {
             this.remove();
            }

        }
    }
}
