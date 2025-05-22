package indi.yunherry.weather.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import indi.yunherry.weather.renderer.ParticleRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class WeatherParticle extends TextureSheetParticle {
    protected BlockPos.MutableBlockPos pos;

    boolean shouldFadeOut = false;
    float temperature;
    protected WeatherParticle(ClientLevel p_108323_, double p_108324_, double p_108325_, double p_108326_) {
        super(p_108323_, p_108324_, p_108325_, p_108326_);
        this.setSize(0.01F, 0.01F);
        //时间尽可能长
        this.lifetime = Integer.MAX_VALUE;
        this.alpha = 0.0F;
        this.pos = new BlockPos.MutableBlockPos(x, y, z);
        this.temperature = level.getBiome(this.pos).value().getBaseTemperature();
        ParticleRenderer.particleCount++;
    }

    @Override
    public void tick() {
        super.tick();
        this.pos.set(this.x, this.y - 0.2, this.z);
//        this.removeIfOOB();
        if (shouldFadeOut) {
            fadeOut();
        } else if (this.age % 10 == 0) {
            if (Mth.abs(level.getBiome(this.pos).value().getBaseTemperature() - this.temperature) > 0.4) shouldFadeOut = true;
        } else {
            fadeIn();
        }
    }
    //使用这个方法后,当生成的位置在视野之外,他就会直接移除粒子
    void removeIfOOB() {
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        if (cameraEntity == null || cameraEntity.distanceToSqr(this.x, this.y, this.z) > Mth.square(25)) {
            shouldFadeOut = true;
        }

    }
    @Override
    public void render(VertexConsumer p_107678_, Camera p_107679_, float p_107680_) {
        super.render(p_107678_, p_107679_, p_107680_);
    }

    @Override
    public void remove() {
        if (this.isAlive()) ParticleRenderer.particleCount--;
        super.remove();
    }

    public void fadeIn() {
        if (age < 20) {
            this.alpha = (age * 1.0f) / 20;
        }
    }

    public void fadeOut() {
        if (this.alpha < 0.01) {
            remove();
        } else {
            this.alpha = this.alpha - 0.05F;
        }
    }

    protected boolean removeIfObstructed() {
        if (x == xo || z == zo) {
            this.remove();
            return true;
        } else {
            return false;
        }
    }
    public Quaternionf flipItTurnwaysIfBackfaced(Quaternionf quaternion, Vector3f toCamera) {
        Vector3f normal = new Vector3f(0, 0, 1);
        normal.rotate(quaternion).normalize();
        float dot = normal.dot(toCamera);
        if (dot > 0) {
            return quaternion.mul(Axis.YP.rotation(Mth.PI));
        }
        else return quaternion;
    }
    public static double yLevelWindAdjustment(double y) {
        return Math.clamp(0.01, 1, (y - 64) / 40);
    }
    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
