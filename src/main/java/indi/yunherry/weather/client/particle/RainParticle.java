package indi.yunherry.weather.client.particle;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import indi.yunherry.weather.RayThreadPool;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

//TODO: Refactor
public class RainParticle {
    public static final float MAX_LENGTH = 32.0F;
    public static final float MAX_WIDTH = 2.0F;
    public static final Map<Biome.Precipitation, ResourceLocation> TEXTURE_BY_PRECIPITATION = Util.make(() -> {
        ImmutableMap.Builder<Biome.Precipitation, ResourceLocation> map = ImmutableMap.builder();
        map.put(Biome.Precipitation.RAIN, new ResourceLocation("textures/environment/rain.png"));
        map.put(Biome.Precipitation.SNOW, new ResourceLocation("textures/environment/snow.png"));
        return map.build();
    });
    private final Biome.Precipitation precipitation;
    //一个对象绑定一个已经写好的位置
    private final AtomicReference<BlockHitResult> hitResult = new AtomicReference<>();
    //在天上生成的起始位置
    private final BlockPos blockPos;
    private final Vector3f position;
    private final int lifeSpan;
    private final float initialWidth;
    private final Function<ClipContext, BlockHitResult> raycaster;
    private float length = MAX_LENGTH;
    private float xRot;
    private float yRot;
    private int tickCount;
    private float widthO;
    private float width;
    private float alpha = 1.0f;
    private class RainInfo {

    }

    public BlockHitResult getHitResult() {
        return hitResult.getAcquire();
    }

    public RainParticle(Biome.Precipitation precipitation, Function<ClipContext, BlockHitResult> raycaster, BlockPos position, float xRot, float yRot, int lifeSpan, float initialWidth) {
        this.precipitation = precipitation;
        this.raycaster = raycaster;
        this.blockPos = position;
        this.position = new Vector3f(position.getX() + 0.5F, position.getY() + 0.5F, position.getZ() + 0.5F);
        this.xRot = xRot;
        this.yRot = yRot;
        this.lifeSpan = lifeSpan;
        this.initialWidth = Math.max(0.1F, initialWidth);// Mth.clamp(initialWidth, 0.1F, MAX_WIDTH);
        Vec3 start = new Vec3(this.position);
        float yawRadians = -this.yRot;
        float pitchRadians = this.xRot - (float) Math.PI / 2.0F;
        float pitchCos = Mth.cos(pitchRadians);
        Vec3 end = new Vec3(Mth.sin(yawRadians) * pitchCos, Mth.sin(pitchRadians), Mth.cos(yawRadians) * pitchCos).scale(MAX_LENGTH).add(start);
        ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, null);
        hitResult.setPlain(this.raycaster.apply(context));
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

        alpha = 1.0f - ((float) this.tickCount / this.lifeSpan);
        Vec3 start = new Vec3(this.position);
        float yawRadians = -this.yRot;
        float pitchRadians = this.xRot - (float) Math.PI / 2.0F;
        //TODO: 不是很优良的解法
        //初始化的时候执行一次,tick后的放到异步执行
        RayThreadPool.submitTask(() -> {
            float pitchCos = Mth.cos(pitchRadians);
            Vec3 end = new Vec3(Mth.sin(yawRadians) * pitchCos, Mth.sin(pitchRadians), Mth.cos(yawRadians) * pitchCos).scale(MAX_LENGTH).add(start);
            ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, null);
            hitResult.setRelease(this.raycaster.apply(context));
        });

        this.length = (float) start.distanceTo(getHitResult().getLocation());
        this.widthO = this.width;
        if (this.tickCount < this.lifeSpan - 20)
            this.width = this.initialWidth * Math.min(1.0F, (float) this.tickCount / 20.0F);
        else
            this.width = this.initialWidth * Math.min(1.0F, ((float) this.lifeSpan - (float) this.tickCount) / 20.0F);
    }

    //TODO: 贴图左右扰动
    //TODO: 跟随风更新角度 about version: 2.1.0-beta
    public void render(PoseStack stack, VertexConsumer consumer, float partialTick, int packedLight, double camX, double camY, double camZ, float r, float g, float b) {
        //平移到指定位置
        stack.translate(this.position.x, this.position.y, this.position.z);
        //创建旋转角度
        Quaternionf inverseRotation = new Quaternionf();
        inverseRotation.rotateX(this.xRot);
        inverseRotation.rotateY(this.yRot);
        Vector3f adjustedCamPos = new Vector3f((float) camX, (float) camY, (float) camZ).sub(this.position).rotate(inverseRotation).add(this.position);
        float angleToCam = (float) Mth.atan2(this.position.x - adjustedCamPos.x, this.position.z - adjustedCamPos.z);
        //旋转
        stack.mulPose(inverseRotation.invert());
        stack.mulPose(Axis.YP.rotation(angleToCam));
        Matrix4f mat = stack.last().pose();
        float vOffset = ((float) this.tickCount + partialTick) * -0.1F;
        float width = Mth.lerp(partialTick, this.widthO, this.width);
        float u1 = width / 2.0F * 0.5F + 0.5F;
        float u0 = 0.5F - width / 2.0F * 0.5F;
        consumer.vertex(mat, width / 2.0F, 0.0F, 0.0F).uv(u0, vOffset).color(r, g, b, 0f).uv2(packedLight).endVertex();
        consumer.vertex(mat, -width / 2.0F, 0.0F, 0.0F).uv(u1, vOffset).color(r, g, b, 0f).uv2(packedLight).endVertex();
        consumer.vertex(mat, -width / 2.0F, -this.length, 0.0F).uv(u1, this.length / 10.0F + vOffset).color(r, g, b, alpha).uv2(packedLight).endVertex();
        consumer.vertex(mat, width / 2.0F, -this.length, 0.0F).uv(u0, this.length / 10.0F + vOffset).color(r, g, b, alpha).uv2(packedLight).endVertex();
    }

}
