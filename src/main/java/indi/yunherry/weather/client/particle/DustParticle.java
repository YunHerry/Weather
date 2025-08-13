package indi.yunherry.weather.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import indi.yunherry.weather.WorldContext;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.Random;

//@OnlyIn(Dist.CLIENT)
//public class DustParticle extends WeatherParticle{
//    protected DustParticle(ClientLevel p_108323_, double p_108324_, double p_108325_, double p_108326_) {
//        super(p_108323_, p_108324_, p_108325_, p_108326_);
//    }
//
//    public WindParticle(ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet p_107724_) {
//        super(world, x, y, z, xSpeed, ySpeed, zSpeed);
//        this.sprites = p_107724_;
//        this.scale(20.5F + random.nextInt(60));
//        this.lifetime = 54;
//        this.hasPhysics = true;
//        this.setPos(x, y, z);
//        this.setSpriteFromAge(p_107724_);
//        this.setAlpha(0.5F);
//        this.rotationY = switch (WorldContext.windDirection) {
//            case NORTH -> 0;
//            case WEST -> 270;
//            case EAST -> 90;
//            case SOUTH -> 180;
//            case NONE -> 114514;
//        } + (random.nextInt(40) - 20);
//    }
//
//    @OnlyIn(Dist.CLIENT)
//    @indi.yunherry.weather.annotation.ParticleProvider(particleName = "dust")
//    public static class Provider implements ParticleProvider<SimpleParticleType> {
//        private final SpriteSet sprite;
//
//        public Provider(SpriteSet p_107739_) {
//            this.sprite = p_107739_;
//        }
//
//        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
//            Random random = new Random();
//            int d = random.nextInt(30) + 40;
//            double r = random.nextDouble() * Math.PI * 2;
//            double newY = y + random.nextInt(15) + random.nextInt(15);
//
//            return new DustParticle(world, (Math.cos(r) * d) + x, newY, (Math.sin(r) * d) + z, xSpeed, ySpeed, zSpeed, this.sprite);
//        }
//    }
//}
