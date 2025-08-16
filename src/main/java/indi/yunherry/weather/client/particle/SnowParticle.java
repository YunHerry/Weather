package indi.yunherry.weather.client.particle;

import indi.yunherry.weather.WindDirectionType;
import indi.yunherry.weather.WorldContext;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SnowParticle extends WeatherParticle {
    private final float rotationAmount;
    private SpriteSet sprites;
    protected SnowParticle(ClientLevel p_108323_, double p_108324_, double p_108325_, double p_108326_,SpriteSet spriteSet) {
        super(p_108323_, p_108324_, p_108325_, p_108326_);
        //水花也需要重写render
        this.sprites = spriteSet;
        this.setSprite(this.sprites.get(RandomSource.create()));
        this.quadSize = Math.max(random.nextFloat()-0.5F,0.3f);
        this.gravity = random.nextFloat() * this.quadSize * (1-this.quadSize);
        this.pos = new BlockPos.MutableBlockPos(x, y, z);
        this.yd = -gravity;
        if (level.isThundering()) {
            this.xd = gravity * 3f;
        } else {
            this.xd = gravity * 1f;
        }
        this.xd = (random.nextFloat() - 0.5) * this.quadSize;
        this.zd = (random.nextFloat() - 0.5)  * this.quadSize;
        if (level.getRandom().nextBoolean()) {
            this.rotationAmount = 1;
        } else {
            this.rotationAmount = -1;
        }
    }

    @Override
    public void tick() {
        if(WorldContext.windDirection != WindDirectionType.NONE) {
            float randomVal = (float) ((random.nextFloat() - 0.5)*0.002);
            this.xd  += switch (WorldContext.windDirection) {
                case NORTH -> 0.001f + randomVal;
                case SOUTH -> -0.001f + randomVal;
                default -> 0;
            };
            this.zd += switch (WorldContext.windDirection) {
                case EAST -> 0.001f + randomVal;
                case WEST -> -0.001f + randomVal;
                default -> 0;
            };
        }
        super.tick();
        this.pos.set(this.x, this.y - 0.2, this.z);
        this.oRoll = this.roll;
        this.roll = this.oRoll + (level.isThundering() ? 0.02f : 0.05f) * this.rotationAmount;
        if (this.onGround || this.removeIfObstructed()) {
            this.remove();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @indi.yunherry.weather.annotation.ParticleProvider(particleName = "snow")
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet p_108492_) {
            this.sprite = p_108492_;
        }

        public Particle createParticle(SimpleParticleType p_108503_, ClientLevel p_108504_, double p_108505_, double p_108506_, double p_108507_, double p_108508_, double p_108509_, double p_108510_) {
            SnowParticle snowParticle = new SnowParticle(p_108504_, p_108505_, p_108506_, p_108507_,sprite);
            return snowParticle;
        }
    }
}
