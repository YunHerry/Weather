package indi.yunherry.weather.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.*;

import java.util.List;
import java.util.function.Function;

import static indi.yunherry.weather.client.Precipitation.MAX_LENGTH;

@Mixin(Particle.class)
public abstract class MixinParticle {
    private static final Logger log = LoggerFactory.getLogger(MixinParticle.class);
    @Shadow protected double xo;
    @Shadow protected double x;
    @Shadow protected double yo;
    @Shadow protected double y;
    @Shadow protected double zo;
    @Shadow protected double z;
    @Shadow protected int age;
    @Shadow protected int lifetime;
    @Shadow @Final protected ClientLevel level;

    @Shadow public abstract void remove();

    @Shadow protected double yd;
    @Shadow protected float gravity;

    @Shadow protected double xd;
    @Shadow protected double zd;
    @Shadow protected boolean speedUpWhenYMotionIsBlocked;
    @Shadow protected float friction;
    @Shadow protected boolean onGround;
    @Unique
    private final Function<ClipContext, BlockHitResult> raycaster = level::clip;
    @Unique
    private float length;
    /**
     * @author
     * @reason
     */
    @Overwrite
    public void tick() {
        Vec3 start = new Vec3(this.x,this.y,this.z);
        Vec3 end = new Vec3(0,-1,0).scale(MAX_LENGTH).add(start);
        ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, null);
        BlockHitResult result = this.raycaster.apply(context);
        Vec3 hit = result.getLocation();
        length = (float)start.distanceTo(hit);
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        log.info(String.valueOf(length));
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.yd =Math.max(this.yd - 0.04D * (double) this.gravity,length);
            this.move(this.xd, this.yd, this.zd);
            if (this.speedUpWhenYMotionIsBlocked && this.y == this.yo) {
             this.xd *= 1.1D;
             this.zd *= 1.1D;
            }

            this.xd *= (double) this.friction;
            this.yd *= (double) this.friction;
            this.zd *= (double) this.friction;
            if (this.onGround) {
             this.xd *= (double) 0.7F;
             this.zd *= (double) 0.7F;
            }

        }
    }
    /**
     * @author
     * @reason
     */
    @Shadow
    private boolean stoppedByCollision;
    @Shadow
    private static final double MAXIMUM_COLLISION_VELOCITY_SQUARED = Mth.square(100.0D);

    @Shadow public abstract AABB getBoundingBox();

    @Shadow protected boolean hasPhysics;

    @Shadow public abstract void setBoundingBox(AABB p_107260_);

    @Shadow protected abstract void setLocationFromBoundingbox();

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void move(double p_107246_, double p_107247_, double p_107248_) {
//        if (!this.stoppedByCollision) {
//            double d0 = p_107246_;
//            double d1 = p_107247_;
//            double d2 = p_107248_;
//            if (this.hasPhysics && (p_107246_ != 0.0D || p_107247_ != 0.0D || p_107248_ != 0.0D) && p_107246_ * p_107246_ + p_107247_ * p_107247_ + p_107248_ * p_107248_ < MAXIMUM_COLLISION_VELOCITY_SQUARED) {
//                Vec3 vec3 = Entity.collideBoundingBox((Entity)null, new Vec3(p_107246_, p_107247_, p_107248_), this.getBoundingBox(), this.level, List.of());
//                p_107246_ = vec3.x;
//                p_107247_ = vec3.y;
//                p_107248_ = vec3.z;
//            }
//
//            if (p_107246_ != 0.0D || p_107247_ != 0.0D || p_107248_ != 0.0D) {
//                this.setBoundingBox(this.getBoundingBox().move(p_107246_, p_107247_, p_107248_));
//                this.setLocationFromBoundingbox();
//            }
//
//            if (Math.abs(d1) >= (double)1.0E-5F && Math.abs(p_107247_) < (double)1.0E-5F) {
//                this.stoppedByCollision = true;
//            }
//
//            this.onGround = d1 != p_107247_ && d1 < 0.0D;
//            if (d0 != p_107246_) {
//                this.xd = 0.0D;
//            }
//
//            if (d2 != p_107248_) {
//                this.zd = 0.0D;
//            }
//
//        }
    }
}
