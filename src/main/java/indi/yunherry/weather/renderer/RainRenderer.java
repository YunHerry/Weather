package indi.yunherry.weather.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import indi.yunherry.weather.ParticleRegistry;
import indi.yunherry.weather.annotation.Renderer;
import indi.yunherry.weather.client.particle.RainParticle;
import indi.yunherry.weather.utils.ShaderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static net.minecraft.client.renderer.LevelRenderer.getLightColor;

@Renderer
public class RainRenderer extends WeatherRenderer {
    private final Map<BlockPos, RainParticle> precipitationQuads = Maps.newHashMap();
    private final Map<Biome.Precipitation, List<RainParticle>> quadsByPrecipitation = Maps.newHashMap();
    private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    @Override
    public void tick() {
        if (mc == null) return;
        if (!level.isRaining()) return;

        float xRot = (12.5f) * ((float) Math.PI / 180.0F);
        Vector3f direction = new Vector3f(1.0F, 0.0F, 0.0F);
        float yRot = (float) -Mth.atan2(direction.x, direction.z);
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

        Biome biome = this.mc.level.getBiome(camPos).value();
        boolean isRainPrecipitation = biome.getPrecipitationAt(camPos) == Biome.Precipitation.RAIN;
        if (isRainPrecipitation) {
            float rainIntensity = this.mc.level.getRainLevel(1.0F);
            //框定范围
            int lifeSpan = level.isThundering() ? 20 : 10;

            Biome.Precipitation precipitation = biome.getPrecipitationAt(pos);
            if (rainIntensity > 0.0F && biome.hasPrecipitation()) {
                ThreadLocalRandom random = ThreadLocalRandom.current();
                for (int x = minX; x < maxX; x++) {
                    for (int z = minZ; z < maxZ; z++) {
                        int height = this.mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                        for (int y = minY; y < maxY; y++) {
                            if (height > y) continue;
                            BlockPos pos = new BlockPos(x, y, z);
                            RandomSource blockRandom = RandomSource.create(pos.asLong());
                            if (!this.precipitationQuads.containsKey(pos)) {
                                if (blockRandom.nextInt(100) <= 1) {
                                    float widthModifier = 2.0F;
                                    RainParticle quad = new RainParticle(precipitation, this.mc.level::clip, pos, xRot + random.nextFloat() * 0.1F, yRot + random.nextFloat() * 0.1F, lifeSpan + random.nextInt(lifeSpan), rainIntensity * widthModifier);
                                    this.precipitationQuads.put(pos, quad);
                                    this.quadsByPrecipitation.computeIfAbsent(precipitation, p -> Lists.newArrayList()).add(quad);
                                }
                            }
                        }
                    }
                }
            }
        }

        AABB box = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        var rain = this.precipitationQuads.entrySet().iterator();
        while (rain.hasNext()) {
            var entry = rain.next();
            RainParticle quad = entry.getValue();
            //渲染雨滴的开始位置
            BlockPos pos = entry.getKey();

            if (!box.contains(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) || quad.isDead()) {
                this.quadsByPrecipitation.get(quad.getPrecipitation()).remove(quad);
                rain.remove();
                continue;
            }

            if (isRainPrecipitation) {
                Level levelreader = this.mc.level;
                BlockHitResult hitResult = quad.getHitResult();
                //when join the world
                if (Objects.nonNull(hitResult) && hitResult.getType() != HitResult.Type.MISS) {
                    //是击中的方块中心
                    BlockPos downPos = hitResult.getBlockPos();
                    BlockState blockstate = levelreader.getBlockState(downPos);
                    FluidState fluidstate = levelreader.getFluidState(downPos);
                    VoxelShape voxelshape = blockstate.getCollisionShape(levelreader, downPos);
                    ThreadLocalRandom random = ThreadLocalRandom.current();
                    ParticleOptions particleoptions = !fluidstate.is(FluidTags.LAVA) && !blockstate.is(Blocks.MAGMA_BLOCK) && !CampfireBlock.isLitCampfire(blockstate) ? ParticleTypes.RAIN : ParticleTypes.SMOKE;
                    double surfaceHeight = voxelshape.max(Direction.Axis.Y, random.nextDouble(), random.nextDouble());
                    double fluidHeight = fluidstate.getHeight(levelreader, downPos);
                    double maxSurfaceY = Math.max(surfaceHeight, fluidHeight);
                    double baseX = downPos.getX() + 0.5;
                    double baseY = downPos.getY() + maxSurfaceY;
                    double baseZ = downPos.getZ() + 0.5;
                    Direction hitFace = quad.getHitResult().getDirection();
                    final double edgeOffset = 0.30; // 外侧偏移量增大防止Z-fighting
                    final double randomSpread = 0.4; // 表面随机散布范围
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
                    if (fluidstate.isSourceOfType(Fluids.WATER) && ThreadLocalRandom.current().nextInt(10000) > 9800) {
                        level.addParticle(ParticleRegistry.RIPPLE.get(), baseX, downPos.getY() + 1, baseZ, 0.0, 0.0, 0.0);
                    }
                }
            }

            quad.tick();
        }

    }

    @Override
    public void render() {

    }

    @Override
    public void renderWeather(LightTexture texture, float partialTick, int ticks) {
        Holder<Biome> biomeHolder = level.getBiome(camPos);
        int color = biomeHolder.value().getWaterColor();
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        if (!level.isRaining()) return;
        float rainIntensity = mc.level.getRainLevel(0.0F);
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
                RenderSystem.setShaderTexture(0, RainParticle.TEXTURE_BY_PRECIPITATION.get(entry.getKey()));
                builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                PoseStack stack = new PoseStack();
                stack.translate(-camPos.getX(), -camPos.getY(), -camPos.getZ());
                for (RainParticle quad : entry.getValue()) {
                    stack.pushPose();
                    int packedLight = getLightColor(this.mc.level, quad.getBlockPos());
                    quad.render(stack, builder, partialTick, packedLight, camPos.getX(), camPos.getY(), camPos.getZ(), r, g, b);
                    stack.popPose();
                }
                tesselator.end();
            }
        }

        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }
}
