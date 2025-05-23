package indi.yunherry.weather.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import indi.yunherry.weather.WeatherConfig;
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
        this.removeAABB();
        if (shouldFadeOut) {
            fadeOut();
        } else if (this.age % 10 == 0) {
            if (Mth.abs(level.getBiome(this.pos).value().getBaseTemperature() - this.temperature) > 0.4) shouldFadeOut = true;
        } else {
            fadeIn();
        }
    }
    void removeAABB() {
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        if (cameraEntity == null || cameraEntity.distanceToSqr(this.x, this.y, this.z) > Mth.square(WeatherConfig.RENDER_RADIUS*2)) {
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
    public static double yLevelWindAdjustment(double y) {
        return Math.clamp(0.01, 1, (y - 64) / 40);
    }
    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
