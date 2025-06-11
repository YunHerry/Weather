package indi.yunherry.weather.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import indi.yunherry.weather.WorldContext;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class WindParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final Quaternionf rotation = new Quaternionf();
    private final Matrix4f transformMatrix = new Matrix4f();
    private final float rotationY;
    public static final ParticleRenderType CUSTOM_SHEET = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager texture) {
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE
            );
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
        }
    };


    public WindParticle(ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet p_107724_) {
        super(world, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = p_107724_;
        this.scale(20.5F + random.nextInt(60));
        this.lifetime = 54;
        this.hasPhysics = true;
        this.setPos(x, y, z);
        this.setSpriteFromAge(p_107724_);
        this.setAlpha(0.5F);
        this.rotationY = switch (WorldContext.windDirection) {
            case NORTH -> 0;
            case WEST -> 270;
            case EAST -> 90;
            case SOUTH -> 180;
            case NONE -> 114514;
        } + (random.nextInt(40) - 20);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return CUSTOM_SHEET;
    }

    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);

        if (this.alpha == 0) remove();
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        // 1. 获取基础参数
        Vec3 camPos = camera.getPosition();
        float size = this.getQuadSize(partialTicks);
        int packedLight = this.getLightColor(partialTicks);
        float alpha = 1;
        //180是南风 90是东风 0是北风
        rotation.identity()
                .rotateY((float) Math.toRadians(-this.rotationY + 180));
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
            Random random = new Random();
            int d = random.nextInt(30) + 40;
            double r = random.nextDouble() * Math.PI * 2;
            double newY = y + random.nextInt(15) + random.nextInt(15);

            return new WindParticle(world, (Math.cos(r) * d) + x, newY, (Math.sin(r) * d) + z, xSpeed, ySpeed, zSpeed, this.sprite);
        }
    }
}