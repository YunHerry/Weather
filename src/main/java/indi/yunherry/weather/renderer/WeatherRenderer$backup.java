package indi.yunherry.weather.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import indi.yunherry.weather.ParticleRegistry;
import indi.yunherry.weather.WeatherType;
import indi.yunherry.weather.WorldContext;
import indi.yunherry.weather.client.RainParticleQuad;
import indi.yunherry.weather.utils.ShaderUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;
//import org.valkyrienskies.mod.common.VSClientGameUtils;
//import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static indi.yunherry.weather.WorldContext.*;
import static net.minecraft.client.renderer.LevelRenderer.getLightColor;

public class WeatherRenderer$backup extends ParticleRenderer {

    // 配置参数
    private static final double AREA_HALF = 32.0; // 半边长16，总区域32x32
    private static final double BASE_HEIGHT = 100.0;
    private static final double HEIGHT_VARIATION = 16.0;
    private static final int MAX_PARTICLES = 2;
    private static final double DENSITY_POWER = 2.0; // 密度衰减曲线强度
    public static WeatherRenderer$backup instance = new WeatherRenderer$backup();
    private Minecraft mc = Minecraft.getInstance();
    public static final Map<Biome.Precipitation, ResourceLocation> TEXTURE_BY_PRECIPITATION = Util.make(() -> {
        ImmutableMap.Builder<Biome.Precipitation, ResourceLocation> map = ImmutableMap.builder();
        map.put(Biome.Precipitation.RAIN, new ResourceLocation("textures/environment/rain.png"));
        map.put(Biome.Precipitation.SNOW, new ResourceLocation("textures/environment/snow.png"));
        return map.build();
    });

//    private final Function<ClipContext, BlockHitResult> raycaster = Minecraft.getInstance().level::clip;
    private static final int HASH_MASK = 0xFFFF; // 控制哈希范围
    private static final float RAIN_SIZE_SCALE = 0.1f; // 控制偏移幅度

    public Map<BlockPos, RainParticleQuad> getPrecipitationQuads() {
        return precipitationQuads;
    }

    private final Map<BlockPos, RainParticleQuad> precipitationQuads = new HashMap<BlockPos, RainParticleQuad>();

    public Map<Biome.Precipitation, List<RainParticleQuad>> getQuadsByPrecipitation() {
        return quadsByPrecipitation;
    }

    private final Map<Biome.Precipitation, List<RainParticleQuad>> quadsByPrecipitation = Maps.newHashMap();
    private static final RandomSource random = RandomSource.create();
    @Deprecated(since = "v1.5")
    //渲染雨
    public void renderWeather(LightTexture texture, float partialTick, double camX, double camY, double camZ, int ticks) {
        float rainIntensity = this.mc.level.getRainLevel(0.0F);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        RenderSystem.depthMask(Minecraft.useShaderTransparency() || ShaderUtils.areShadersRunning());
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        if (!this.quadsByPrecipitation.isEmpty()) {
            //这里是操作雨的贴图
            texture.turnOnLightLayer();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getParticleShader);
            for (var entry : this.quadsByPrecipitation.entrySet()) {
                RenderSystem.setShaderTexture(0, RainParticleQuad.TEXTURE_BY_PRECIPITATION.get(entry.getKey()));
                builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                PoseStack stack = new PoseStack();
                stack.translate(-camX, -camY, -camZ);
                for (RainParticleQuad quad : entry.getValue()) {
                    stack.pushPose();
                    int packedLight = getLightColor(this.mc.level, quad.getBlockPos());
                    quad.render(stack, builder, partialTick, packedLight, camX, camY, camZ, rainIntensity, ticks);
                    stack.popPose();
                }
                tesselator.end();
            }
//            RenderSystem.enableCull();
        }

        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    private static void startEasing(float currentAngle) {
        isEasing = true;
        easeStartAngle = currentAngle;
        easeTargetAngle = currentAngle + (random.nextBoolean() ? 1 : -1) * random.nextFloat() * MAX_EASE_OFFSET;
        easeDuration = Mth.nextInt(random, MIN_EASE_DURATION, MAX_EASE_DURATION);
        easeTimer = 0;
    }

    private static void updateEasing(float partialTicks) {
        easeTimer += partialTicks;

        if (easeTimer >= easeDuration) {
            isEasing = false;
        }
    }

    public void tick() {
//        RenderSystem.enableCull();
        if (nowWeather == WeatherType.NONE) return;
        if (nowWeather == WeatherType.RAIN) {
            float rainIntensity = this.mc.level.getRainLevel(0.0F);
            //angle
            float xRot = (12.5f) * ((float) Math.PI / 180.0F);
            Vector3f direction = new Vector3f(1.0F, 0.0F, 0.0F);
            float yRot = (float) -Mth.atan2((double) direction.x, (double) direction.z);
            float xRotCos = Mth.cos(xRot - (float) Math.PI / 2.0F);
            int xOffset = Mth.floor(Mth.sin(-yRot) * xRotCos * ((float) 32 / 2.0F));
            int zOffset = Mth.floor(Mth.cos(-yRot) * xRotCos * ((float) 32 / 2.0F));
            int radius = Mth.floor((float) 32 / 2.0F * (Minecraft.useFancyGraphics() ? 1.0F : 0.5F));

            int minX = camPos.getX() - radius - xOffset;
            int minY = camPos.getY() + 8;
            int minZ = camPos.getZ() - radius - zOffset;
            int maxX = camPos.getX() + radius - xOffset;
            //TODO configurable
            int maxY = camPos.getY() + 32;
            int maxZ = camPos.getZ() + radius - zOffset;
            //框定范围
            AABB box = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
            Biome biome = this.mc.level.getBiome(camPos).value();
            if (rainIntensity > 0.0F && biome.hasPrecipitation()) {
                //渲染粒子的视角范围内去检查最高的阻挡方块
                for (int x = minX; x < maxX; x++) {
                    for (int z = minZ; z < maxZ; z++) {
                        int height = this.mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                        for (int y = minY; y < maxY; y++) {
                            if (height > y) continue;
                            //render start position
                            BlockPos pos = new BlockPos(x, y, z);
//                        Biome.Precipitation precipitation = biome.getPrecipitationAt(pos);
                            Biome.Precipitation precipitation = Biome.Precipitation.RAIN;
                            RandomSource blockRandom = RandomSource.create(pos.asLong());
                            if (!this.precipitationQuads.containsKey(pos)) {
                                if (blockRandom.nextInt(100) <= 1) {
                                    float widthModifier = precipitation == Biome.Precipitation.SNOW ? 4.0F : 2.0F;
                                    RainParticleQuad quad = new RainParticleQuad(precipitation, this.mc.level::clip, pos, xRot + this.random.nextFloat() * 0.1F, yRot + this.random.nextFloat() * 0.1F, 60 + this.random.nextInt(60), rainIntensity * widthModifier);
//                                    mc.level.addParticle(ParticleRegistry.SNOW.get(),camX - d0 + 0.5D, camY, camZ - d1 + 0.5D,0,1,0);
                                    this.precipitationQuads.put(pos, quad);

                                    this.quadsByPrecipitation.computeIfAbsent(precipitation, p -> Lists.newArrayList()).add(quad);
                                }
                            }
                        }
                    }
                }
            }
            var rain = this.precipitationQuads.entrySet().iterator();
            while (rain.hasNext()) {
                var entry = rain.next();
                Level levelreader = this.mc.level;
                RainParticleQuad quad = entry.getValue();
                //渲染雨滴的开始位置
                BlockPos pos = entry.getKey();
                //是否超出视野,或者是是否已经落地,没有就继续执行

                if (!box.contains(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) || quad.isDead()) {
                    this.quadsByPrecipitation.get(quad.getPrecipitation()).remove(quad);
                    rain.remove();
                } else {
                    quad.tick();
                }
            }
        }
        final RandomSource rand = RandomSource.create();
        final Vec3 camPosition = camPos.getCenter();
        final Level level = mc.level;

        int PARTICLE_RADIUS = 25;
        int MAX_PARTICLES = 80; // 减少总数但提升有效生成率
        double CENTER_BIAS = 2.5; // 中心分布强度
        // 使用半球状分布代替全球
        if (nowWeather == WeatherType.SNOW) {
            for (int i = 0; i < MAX_PARTICLES; i++) {
                // 生成偏向中心的分布
                double theta = rand.nextDouble() * 2 * Math.PI;
                double phi = Math.acos(Math.pow(rand.nextDouble(), CENTER_BIAS)); // 使用幂次分布

                // 转换为笛卡尔坐标（Y轴向上）
                double radius1 = PARTICLE_RADIUS * Math.pow(rand.nextDouble(), 0.6); // 半径向中心偏移
                double x = radius1 * Math.sin(phi) * Math.cos(theta);
                double z = radius1 * Math.sin(phi) * Math.sin(theta);
                double y = radius1 * Math.cos(phi); // 半球只需正值

                // 应用位置偏移
                double posX = x + camPosition.x;
                double posY = y + camPosition.y + 5; // 从玩家上方5格开始
                double posZ = z + camPosition.z;

                // 快速高度检查
                if (posY < level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY() || posY > camPosition.y + PARTICLE_RADIUS) {
                    continue;
                }
                spawnParticle((ClientLevel) level, level.getBiome(pos), posX, posY + rand.nextFloat(), posZ);
            }
        }
        if (WorldContext.nowWeather == WeatherType.SNOW) return;
        this.precipitationQuads.forEach((key, value) -> {
            Level levelreader = this.mc.level;
            BlockPos downPos = value.getDownBlockPos();
            if (downPos != null) {
                BlockState blockstate = levelreader.getBlockState(downPos);
                FluidState fluidstate = levelreader.getFluidState(downPos);
                VoxelShape voxelshape = blockstate.getCollisionShape(levelreader, downPos);
                double d0 = random.nextDouble();
                double d1 = random.nextDouble();
                double d2 = voxelshape.max(Direction.Axis.Y, d0, d1);
                double d3 = (double) fluidstate.getHeight(levelreader, downPos);
                double d4 = Math.max(d2, d3);
                ParticleOptions particleoptions = !fluidstate.is(FluidTags.LAVA) && !blockstate.is(Blocks.MAGMA_BLOCK) && !CampfireBlock.isLitCampfire(blockstate) ? ParticleTypes.RAIN : ParticleTypes.SMOKE;
                // 基础坐标（方块中心）
                // 获取方块顶部表面高度（考虑碰撞体积）
                double surfaceHeight = voxelshape.max(Direction.Axis.Y, random.nextDouble(), random.nextDouble());
// 考虑流体高度（如水上表面）
                double fluidHeight = fluidstate.getHeight(levelreader, downPos);
                double maxSurfaceY = Math.max(surfaceHeight, fluidHeight);
                double baseX = downPos.getX() + 0.5;
                double baseY = downPos.getY() + maxSurfaceY;
                double baseZ = downPos.getZ() + 0.5;

// 碰撞面方向处理
                Direction hitFace = value.getHitDirection();
                final double edgeOffset = 0.15; // 外侧偏移量增大防止Z-fighting
                final double randomSpread = 0.4; // 表面随机散布范围

// 根据碰撞面调整坐标
                switch (hitFace) {
                    case UP -> {
                        // 顶部表面：在XY平面随机散布，Y轴位于表面上方
                        baseX += (random.nextDouble() - 0.5) * randomSpread;
                        baseZ += (random.nextDouble() - 0.5) * randomSpread;
                        baseY += edgeOffset; // 确保在方块上方
                    }
                    case DOWN -> {
                        // 底部不生成粒子
                        return;
                    }
                    case NORTH -> {
                        // 北侧：Z轴负方向偏移，XY平面随机
                        baseZ = downPos.getZ() - edgeOffset;
                        baseX += random.nextDouble() * randomSpread;
                    }
                    case SOUTH -> {
                        // 南侧：Z轴正方向偏移
                        baseZ = downPos.getZ() + 1 + edgeOffset;
                        baseX += random.nextDouble() * randomSpread;
                    }
                    case EAST -> {
                        // 东侧：X轴正方向偏移
                        baseX = downPos.getX() + 1 + edgeOffset;
                        baseZ += random.nextDouble() * randomSpread;
                    }
                    case WEST -> {
                        // 西侧：X轴负方向偏移
                        baseX = downPos.getX() - edgeOffset;
                        baseZ += random.nextDouble() * randomSpread;
                    }
                }

                levelreader.addParticle(particleoptions, baseX, baseY + (hitFace == Direction.UP ? 0 : random.nextDouble() * 0.3), baseZ, 0.0, 0.0, 0.0);
            }
        });
    }

    @Override
    public void render() {

    }

    @Override
    public boolean isRender() {
        return false;
    }


    private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    private static void spawnParticle(ClientLevel level, Holder<Biome> biome, double x, double y, double z) {
        if (particleCount > maxParticleCount) {
            return;
        }
        //从云的高度下生成
        y = y + 8;
        final BlockPos getPrecipitationFromBlockPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos);
        if (level.random.nextFloat() < 0.8f) {
            level.addParticle(ParticleRegistry.SNOW.get(), x, y, z, 0, 0, 0);
        }
    }

}
