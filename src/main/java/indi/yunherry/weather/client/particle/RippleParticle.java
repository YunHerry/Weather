package indi.yunherry.weather.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import indi.yunherry.weather.WorldContext;
import indi.yunherry.weather.renderer.ParticleRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RippleParticle extends WeatherParticle {
    private final SpriteSet sprites;
    private final Quaternionf rotation = new Quaternionf();
    private final Matrix4f transformMatrix = new Matrix4f();
    private static final List<Long> key = new ArrayList<>(10);
    public static Long getXYZKey(double x, double y, double z) {
        return Double.doubleToLongBits(x) + Double.doubleToLongBits(y) + Double.doubleToLongBits(z);
    }
    public static boolean isContain(double x, double y, double z) {
        return key.contains(getXYZKey(x, y, z));
    }
    public RippleParticle(ClientLevel world, double x, double y, double z, SpriteSet p_107724_) {
        super(world, x, y, z);
        this.sprites = p_107724_;
        this.scale(random.nextFloat()+1);
        this.lifetime = 20;
//        this.hasPhysics = true;
        this.setPos(x, y, z);
        this.setSpriteFromAge(p_107724_);
//        this.setAlpha(0.5F);
        key.add(getXYZKey(x, y, z));
    }
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        if (this.alpha == 0) remove();
    }
    @Override
    public void remove() {
        if (this.isAlive()) key.remove(getXYZKey(x, y, z));
        super.remove();
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        // 1. 获取基础参数
        Vec3 camPos = camera.getPosition();
        float size = this.getQuadSize(partialTicks);
        int packedLight = this.getLightColor(partialTicks);
        float alpha = (float) (0.6f * Math.sin(Math.PI * this.age / 20));;
        //180是南风 90是东风 0是北风
        rotation.identity()
                .rotateX((float) Math.toRadians(90));
        // 3. 构建变换矩阵
        transformMatrix.identity()
                .translation(
                        (float) (this.x - camPos.x + xd * partialTicks),
                        (float) (this.y - camPos.y + yd * partialTicks),
                        (float) (this.z - camPos.z + zd * partialTicks)
                )
                .rotate(rotation)
                .scale(size);
        renderQuad(buffer, packedLight, alpha);
    }


    private void renderQuad(VertexConsumer buffer, int packedLight, float alpha) {
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();

        buffer.vertex(transformMatrix, -1, -1, 0)
                .uv(u1, v1)
                .color(rCol, gCol, bCol, alpha)
                .uv2(packedLight)
                .endVertex();

        buffer.vertex(transformMatrix, -1, 1, 0)
                .uv(u1, v0)
                .color(rCol, gCol, bCol, alpha)
                .uv2(packedLight)
                .endVertex();

        buffer.vertex(transformMatrix, 1, 1, 0)
                .uv(u0, v0)
                .color(rCol, gCol, bCol, alpha)
                .uv2(packedLight)
                .endVertex();

        buffer.vertex(transformMatrix, 1, -1, 0)
                .uv(u0, v1)
                .color(rCol, gCol, bCol, alpha)
                .uv2(packedLight)
                .endVertex();
    }


    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet p_107739_) {
            this.sprite = p_107739_;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new RippleParticle(world, x, y, z, this.sprite);
        }
    }
}
