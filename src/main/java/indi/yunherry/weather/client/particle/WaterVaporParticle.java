package indi.yunherry.weather.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
@OnlyIn(Dist.CLIENT)
public class WaterVaporParticle extends TextureSheetParticle {
    WaterVaporParticle(ClientLevel p_105856_, double p_105857_, double p_105858_, double p_105859_, double p_105860_, double p_105861_, double p_105862_, boolean p_105863_) {
        super(p_105856_, p_105857_, p_105858_, p_105859_);
        this.scale(3.0F);
        this.setSize(0.25F, 0.25F);
        if (p_105863_) {
            this.lifetime = this.random.nextInt(50) + 150;
        } else {
            this.lifetime = this.random.nextInt(50) + 50;
        }

        this.gravity = 3.0E-6F;
        this.xd = p_105860_;
        this.yd = p_105861_ + (double)(this.random.nextFloat() / 500.0F);
        this.zd = p_105862_;
    }

    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ < this.lifetime && !(this.alpha <= 0.0F)) {
            this.xd += (double)(this.random.nextFloat() / 5000.0F * (float)(this.random.nextBoolean() ? 1 : -1));
            this.zd += (double)(this.random.nextFloat() / 5000.0F * (float)(this.random.nextBoolean() ? 1 : -1));
            this.yd -= (double)this.gravity;
            this.move(this.xd, this.yd, this.zd);
            if (this.age >= this.lifetime - 60 && this.alpha > 0.01F) {
                this.alpha -= 0.015F;
            }

        } else {
            this.remove();
        }
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
    
    @OnlyIn(Dist.CLIENT)
    @indi.yunherry.weather.annotation.ParticleProvider(particleName = "water_vapor")
    public static class SignalProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public SignalProvider(SpriteSet p_105899_) {
            this.sprites = p_105899_;
        }

        public Particle createParticle(SimpleParticleType p_105910_, ClientLevel p_105911_, double p_105912_, double p_105913_, double p_105914_, double p_105915_, double p_105916_, double p_105917_) {
            WaterVaporParticle smokeParticle = new WaterVaporParticle(p_105911_, p_105912_, p_105913_, p_105914_, p_105915_, p_105916_, p_105917_, true);
            smokeParticle.setAlpha(0.95F);
            smokeParticle.pickSprite(this.sprites);
            return smokeParticle;
        }
    }
}
