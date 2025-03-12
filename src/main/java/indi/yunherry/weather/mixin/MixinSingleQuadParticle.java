package indi.yunherry.weather.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Function;

import static indi.yunherry.weather.client.Precipitation.MAX_LENGTH;

@Mixin(SingleQuadParticle.class)
public abstract class MixinSingleQuadParticle extends Particle {

    private static final Logger log = LoggerFactory.getLogger(MixinSingleQuadParticle.class);

    protected MixinSingleQuadParticle(ClientLevel p_107234_, double p_107235_, double p_107236_, double p_107237_) {
        super(p_107234_, p_107235_, p_107236_, p_107237_);
    }

    @Shadow public abstract float getQuadSize(float p_107681_);

    @Shadow protected abstract float getU0();

    @Shadow protected abstract float getU1();

    @Shadow protected abstract float getV0();

    @Shadow protected abstract float getV1();

    @Shadow public abstract void render(VertexConsumer p_107678_, Camera p_107679_, float p_107680_);
    @Unique
    private final Function<ClipContext, BlockHitResult> raycaster = level::clip;
    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true
    )
    //TODO 漏雨
    public void render(VertexConsumer p_107678_, Camera p_107679_, float p_107680_, CallbackInfo ci) {
        Vec3 start = new Vec3(this.x,this.y,this.z);
        Vec3 end = new Vec3(0,-1,0).scale(MAX_LENGTH).add(start);
        ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, null);
        BlockHitResult result = this.raycaster.apply(context);
        Vec3 hit = result.getLocation();
        float length = result.getBlockPos().getY();
//        log.info(String.valueOf(length));
//        this.yo = 85;
        this.y = Math.max(length,this.y);
//        this.yo = Math.max(length,this.yo);
        Vec3 vec3 = p_107679_.getPosition();
        float f = (float)(Mth.lerp((double)p_107680_, this.xo, this.x) - vec3.x());
        float f1 = (float)(Mth.lerp((double)p_107680_, this.yo, this.y) - vec3.y());
        float f2 = (float)(Mth.lerp((double)p_107680_, this.zo, this.z) - vec3.z());
        Quaternionf quaternionf;
        if (this.roll == 0.0F) {
            quaternionf = p_107679_.rotation();
        } else {
            quaternionf = new Quaternionf(p_107679_.rotation());
            quaternionf.rotateZ(Mth.lerp(p_107680_, this.oRoll, this.roll));
        }


        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f3 = this.getQuadSize(p_107680_);

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.rotate(quaternionf);
            vector3f.mul(f3);
            vector3f.add(f, f1, f2);
        }

        float f6 = this.getU0();
        float f7 = this.getU1();
        float f4 = this.getV0();
        float f5 = this.getV1();
        int j = this.getLightColor(p_107680_);
//        log.info("y: " + this.y + " yo: " + this.yo);
//        log.info("0: " + avector3f[0].y() + " 1: " + avector3f[1].y() + " 2: " + avector3f[2].y() + " 3: " + avector3f[3].y());
        //这俩属性控制下落
//        this.yo = 85;
//        log.info(String.valueOf(this.y));
        p_107678_.vertex((double)avector3f[0].x(), (double)avector3f[0].y(), (double)avector3f[0].z()).uv(f7, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        p_107678_.vertex((double)avector3f[1].x(), (double)avector3f[1].y(), (double)avector3f[1].z()).uv(f7, f4).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        p_107678_.vertex((double)avector3f[2].x(), (double)avector3f[2].y(), (double)avector3f[2].z()).uv(f6, f4).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        p_107678_.vertex((double)avector3f[3].x(), (double)avector3f[3].y(), (double)avector3f[3].z()).uv(f6, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();

        ci.cancel();

    }

    /**
     * @author
     * @reason
     */

}
