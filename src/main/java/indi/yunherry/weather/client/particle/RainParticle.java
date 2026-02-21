package indi.yunherry.weather.client.particle;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import indi.yunherry.weather.AnimationController;
import indi.yunherry.weather.GlobalContext;
import indi.yunherry.weather.RayThreadPool;
import indi.yunherry.weather.compact.create.CreateRayUtils;
import indi.yunherry.weather.utils.ShaderUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static indi.yunherry.weather.Weather.MOD_ID;

//TODO: Refactor
public class RainParticle {
    public static final float MAX_LENGTH = 32.0F;
    public static final float MAX_WIDTH = 2.0F;
    public static final Map<Biome.Precipitation, ResourceLocation> TEXTURE_BY_PRECIPITATION = Util.make(() -> {
        ImmutableMap.Builder<Biome.Precipitation, ResourceLocation> map = ImmutableMap.builder();
        map.put(Biome.Precipitation.RAIN, new ResourceLocation("textures/environment/rain.png"));
//        map.put(Biome.Precipitation.RAIN, new ResourceLocation(MOD_ID, "textures/environment/test.png"));
        map.put(Biome.Precipitation.SNOW, new ResourceLocation("textures/environment/snow.png"));

        return map.build();
    });

    public int getRainSoundTime() {
        return rainSoundTime++;
    }

    public void resetRainSoundTime() {
        rainSoundTime = 0;
    }

    private int rainSoundTime;
    private static final Logger log = LoggerFactory.getLogger(RainParticle.class);
    private final Biome.Precipitation precipitation;
    //一个对象绑定一个已经写好的位置
    private final AtomicReference<BlockHitResult> hitResult = new AtomicReference<>();
    //在天上生成的起始位置
    private final BlockPos blockPos;
    //代表粒子的生成位置
    private final Vector3f position;
    private final int lifeSpan;
    private final float initialWidth;
    private final Function<ClipContext, BlockHitResult> raycaster;
    private AtomicReference<Float> length = new AtomicReference<>(MAX_LENGTH);
    private float xRot;
    private float yRot;
    private float zRot;
    private int tickCount;
    private float widthO;
    private float width;
    private float alpha;
    private int rayLength;

    public BlockHitResult getHitResult() {
        return hitResult.getAcquire();
    }

    public RainParticle(Biome.Precipitation precipitation, Function<ClipContext, BlockHitResult> raycaster, BlockPos position, float xRot, float yRot, float zRot, int lifeSpan, float initialWidth, Level level) {
        this.precipitation = precipitation;
        this.raycaster = raycaster;
        this.blockPos = position;
        this.position = new Vector3f(position.getX() + 0.5F, position.getY() + 0.5F, position.getZ() + 0.5F);
        this.xRot = xRot;
        this.yRot = yRot;
        this.zRot = zRot;
        this.lifeSpan = lifeSpan;
        float rainLevel = (float) GlobalContext.getLoaderConfig().rain();
        this.initialWidth = Mth.lerp(rainLevel, 0.6f, 1.6f); // 小雨0.6，大雨1.6
        float yawRadians = -this.yRot;
        float pitchRadians = this.xRot - (float) Math.PI / 2.0F;
        float pitchCos = Mth.cos(pitchRadians);
        this.alpha = ShaderUtils.areShadersRunning() ? 1.0f : 0.5f;
        Vec3 reverseDir = new Vec3(-Mth.sin(yawRadians) * pitchCos, -Mth.sin(pitchRadians), -Mth.cos(yawRadians) * pitchCos).normalize();

        double particleY = position.getY() + 0.5;
        if (reverseDir.y == 0) {
            hitResult.setPlain(BlockHitResult.miss(new Vec3(position.getX() + 0.5, particleY, position.getZ() + 0.5), Direction.UP, position));
            return;
        }

        // start：从粒子沿 reverseDir 延伸到 y=255
        double deltaY = 255.0 - particleY;
        double scaleUp = deltaY / reverseDir.y;
        Vec3 particlePos = new Vec3(position.getX() + 0.5, particleY, position.getZ() + 0.5);
        Vec3 start = particlePos.add(reverseDir.scale(scaleUp));

        // end：从粒子沿雨落方向（-reverseDir）延伸 MAX_LENGTH，确保能打到粒子下方的水面
        Vec3 forwardDir = reverseDir.scale(-1.0);
        Vec3 end = particlePos.add(forwardDir.scale(MAX_LENGTH));

        ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, null);
        hitResult.setPlain(this.raycaster.apply(context));

        BlockHitResult initialHit = hitResult.getAcquire();
        if (initialHit.getType() != HitResult.Type.MISS) {
            double hitY = initialHit.getLocation().y;
            BlockPos hitBlockPos = initialHit.getBlockPos();
            FluidState fs = level.getFluidState(hitBlockPos);
            if (!fs.isEmpty()) {
                hitY = hitBlockPos.getY() + fs.getHeight(level, hitBlockPos);
            }
            float initialLength = (float)(this.position.y - hitY);
            this.length.set(Math.max(0.0F, initialLength - 0.15F));
        }
    }

    public Biome.Precipitation getPrecipitation() {
        return this.precipitation;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public boolean isDead() {
        return this.tickCount > this.lifeSpan;
    }

    public void tick() {
        this.tickCount++;
        float rainLevel = (float) GlobalContext.getLoaderConfig().rain();
        alpha = (1f - ((float) this.tickCount / this.lifeSpan))
                * Mth.clamp((float) GlobalContext.getLoaderConfig().rain(), 0.2f, 1.0f);
//        alpha = (float) Math.pow(GlobalContext.getLoaderConfig().rain(),1.2);
        //TODO: 不是很优良的解法
        //初始化的时候执行一次,tick后的放到异步执行
        RayThreadPool.submitTask(() -> {
            Vec3 origin = new Vec3(position.x + 0.5, position.y + 0.5, position.z + 0.5);
            float yawRadians = -this.yRot;
            float pitchRadians = this.xRot - (float) Math.PI / 2.0F;
            float pitchCos = Mth.cos(pitchRadians);

            Vec3 direction = new Vec3(Mth.sin(yawRadians) * pitchCos, Mth.sin(pitchRadians), Mth.cos(yawRadians) * pitchCos).normalize();
            Vec3 end = origin.add(direction.scale(128.0));
            BlockHitResult result = CreateRayUtils.clipWithContraptions(GlobalContext.level, origin, end);
            Vec3 hit = result.getType() == HitResult.Type.MISS ? end : result.getLocation();
            float distance = (float) origin.distanceTo(hit);
            this.length.setRelease(distance - 0.15F);
            this.hitResult.setRelease(result);

        });

        this.widthO = this.width;
        if (this.tickCount < this.lifeSpan - 20)
            this.width = this.initialWidth * Math.min(1.0F, (float) this.tickCount / 20.0F);
        else this.width = this.initialWidth * Math.min(1.0F, ((float) this.lifeSpan - (float) this.tickCount) / 20.0F);

    }

    //TODO: 贴图左右扰动
    //TODO: 跟随风更新角度 about version: 2.1.0-beta
    public void render(PoseStack stack, VertexConsumer consumer, float partialTick, int packedLight, double camX, double camY, double camZ, float r, float g, float b) {

        //平移到指定位置
        stack.translate(this.position.x, this.position.y, this.position.z);
        //创建旋转角度
        Quaternionf inverseRotation = new Quaternionf();
        inverseRotation.rotateX(this.xRot);
        inverseRotation.rotateZ(this.zRot);
        inverseRotation.rotateY(this.yRot);
        Vector3f adjustedCamPos = new Vector3f((float) camX, (float) camY, (float) camZ).sub(this.position).rotate(inverseRotation).add(this.position);
        float angleToCam = (float) Mth.atan2(this.position.x - adjustedCamPos.x, this.position.z - adjustedCamPos.z);
        //旋转
//        float animationTick = AnimationController.getAnimationPartialTick(partialTick);
        stack.mulPose(inverseRotation.invert());
        stack.mulPose(Axis.YP.rotation(angleToCam));
        Matrix4f mat = stack.last().pose();
        float rainSpeed = Mth.clamp((float) GlobalContext.getLoaderConfig().rain(), 0.3f, 1.0f);
        float vOffset = ((float) this.tickCount + partialTick) * -rainSpeed * 0.1f;
        float width = Mth.lerp(partialTick, this.widthO, this.width);
        float u1 = width / 2.0F * 0.5F + 0.5F;
        float u0 = 0.5F - width / 2.0F * 0.5F;
        consumer.vertex(mat, width / 2.0F, 0.0F, 0.0F).uv(u0, vOffset).color(r, g, b, 0f).uv2(packedLight).endVertex();
        consumer.vertex(mat, -width / 2.0F, 0.0F, 0.0F).uv(u1, vOffset).color(r, g, b, 0f).uv2(packedLight).endVertex();
        consumer.vertex(mat, -width / 2.0F, -this.length.getAcquire(), 0.0F).uv(u1, this.length.getAcquire() / 10.0F + vOffset).color(r, g, b, alpha).uv2(packedLight).endVertex();
        consumer.vertex(mat, width / 2.0F, -this.length.getAcquire(), 0.0F).uv(u0, this.length.getAcquire() / 10.0F + vOffset).color(r, g, b, alpha).uv2(packedLight).endVertex();

    }

}
