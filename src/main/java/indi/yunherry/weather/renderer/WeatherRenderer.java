package indi.yunherry.weather.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import indi.yunherry.weather.WorldContext;
import indi.yunherry.weather.client.Precipitation;
import indi.yunherry.weather.util.ShaderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeatherRenderer {

    private static final Logger log = LoggerFactory.getLogger(WeatherRenderer.class);
    public static WeatherRenderer instance = new WeatherRenderer();
    private Minecraft mc = Minecraft.getInstance();
    public Map<BlockPos, Precipitation> getPrecipitationQuads() {
        return precipitationQuads;
    }

    private final Map<BlockPos, Precipitation> precipitationQuads = new HashMap<BlockPos, Precipitation>();

    public Map<Biome.Precipitation, List<Precipitation>> getQuadsByPrecipitation() {
        return quadsByPrecipitation;
    }

    private final Map<Biome.Precipitation, List<Precipitation>> quadsByPrecipitation = Maps.newHashMap();
    private final RandomSource random = RandomSource.create();

    public void renderWeather(LightTexture texture, float partialTick, double camX, double camY, double camZ)
    {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        RenderSystem.depthMask(Minecraft.useShaderTransparency() || ShaderUtils.areShadersRunning());
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();

        if (!this.quadsByPrecipitation.isEmpty())
        {
            //这里是操作雨的贴图
            texture.turnOnLightLayer();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getParticleShader);
            for (var entry : this.quadsByPrecipitation.entrySet())
            {
                RenderSystem.setShaderTexture(0, Precipitation.TEXTURE_BY_PRECIPITATION.get(entry.getKey()));
                builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                PoseStack stack = new PoseStack();
                stack.translate(-camX, -camY, -camZ);
                for (Precipitation quad : entry.getValue())
                {
                    //render
                    stack.pushPose();
                    int packedLight = LevelRenderer.getLightColor(this.mc.level, quad.getBlockPos());
                    quad.render(stack, builder, partialTick, packedLight, camX, camY, camZ);
                    stack.popPose();
                }
                tesselator.end();
            }
            RenderSystem.enableCull();
        }

        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }
    public void tick()
    {
        float rainIntensity = this.mc.level.getRainLevel(0.0F);
        BlockPos camPos = this.mc.gameRenderer.getMainCamera().getBlockPosition();
        //angle
        float xRot = (12.5f) * ((float)Math.PI / 180.0F);
        Vector3f direction = new Vector3f(1.0F, 0.0F, 0.0F);;
        float yRot = (float)-Mth.atan2((double)direction.x, (double)direction.z);
        float xRotCos = Mth.cos(xRot - (float)Math.PI / 2.0F);
        int xOffset = Mth.floor(Mth.sin(-yRot) * xRotCos * ((float)32 / 2.0F));
        int zOffset = Mth.floor(Mth.cos(-yRot) * xRotCos * ((float)32 / 2.0F));
        int radius = Mth.floor((float)32 / 2.0F * (Minecraft.useFancyGraphics() ? 1.0F : 0.5F));
        int minX = camPos.getX() - radius - xOffset;
        int minY = camPos.getY() + 8;
        int minZ = camPos.getZ() - radius - zOffset;
        int maxX = camPos.getX() + radius - xOffset;
        //TODO configable
        int maxY = camPos.getY() + 32;
        int maxZ = camPos.getZ() + radius - zOffset;
        //框定范围
        AABB box = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        Biome biome = this.mc.level.getBiome(camPos).value();
        if (rainIntensity > 0.0F && biome.hasPrecipitation())
        {
            //渲染粒子的视角范围内去检查最高的阻挡方块
            for (int x = minX; x < maxX; x++)
            {
                for (int z = minZ; z < maxZ; z++)
                {
                    int height = this.mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                    for (int y = minY; y < maxY; y++)
                    {
                        if (height > y)
                            continue;
                        //render start position
                        BlockPos pos = new BlockPos(x, y, z);
//                        Biome.Precipitation precipitation = biome.getPrecipitationAt(pos);
                        Biome.Precipitation precipitation = WorldContext.nowWeather.equals("rain")? Biome.Precipitation.RAIN: Biome.Precipitation.SNOW;
                        RandomSource blockRandom = RandomSource.create(pos.asLong());
                        if (!this.precipitationQuads.containsKey(pos))
                        {
                            if (blockRandom.nextInt(100) <= 2)
                            {
                                float widthModifier = precipitation == Biome.Precipitation.SNOW ? 4.0F : 2.0F;
                                Precipitation quad = new Precipitation(precipitation, this.mc.level::clip, pos, xRot + this.random.nextFloat() * 0.1F, yRot + this.random.nextFloat() * 0.1F, 60 + this.random.nextInt(60), rainIntensity * widthModifier);
                                this.precipitationQuads.put(pos, quad);
                                this.quadsByPrecipitation.computeIfAbsent(precipitation, p -> Lists.newArrayList()).add(quad);
                            }
                        }
                    }
                }
            }
        }

        var rain = this.precipitationQuads.entrySet().iterator();
        while (rain.hasNext())
        {
            var entry = rain.next();
            Level levelreader = this.mc.level;
            Precipitation quad = entry.getValue();
            //渲染雨滴的开始位置
            BlockPos pos = entry.getKey();
            //是否超出视野,或者是是否已经落地,没有就继续执行

            if (!box.contains(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) || quad.isDead())
            {
                this.quadsByPrecipitation.get(quad.getPrecipitation()).remove(quad);
                rain.remove();
            }
            else
            {
                quad.tick();
            }

        }
        //fix physics block
        if(WorldContext.nowWeather.equals("snow")) return;
        this.precipitationQuads.forEach((key, value) -> {
            Level levelreader = this.mc.level;
            BlockPos downPos = value.getDownBlockPos();
            if (downPos != null) {
                Vec3 downLocation = value.getDownPos();
//                if (Math.abs(downPos.getX()) > 20000000) return;
                BlockState blockstate = levelreader.getBlockState(downPos);
                FluidState fluidstate = levelreader.getFluidState(downPos);
                VoxelShape voxelshape = blockstate.getCollisionShape(levelreader, downPos);
                double d0 = random.nextDouble();
                double d1 = random.nextDouble();
                double d2 = voxelshape.max(Direction.Axis.Y, d0, d1);
                double d3 = (double) fluidstate.getHeight(levelreader, downPos);
                double d4 = Math.max(d2, d3);
                ParticleOptions particleoptions = !fluidstate.is(FluidTags.LAVA) && !blockstate.is(Blocks.MAGMA_BLOCK) && !CampfireBlock.isLitCampfire(blockstate) ? ParticleTypes.RAIN : ParticleTypes.SMOKE;
                double particleX = downPos.getX() + 0.5;
                double particleY = downPos.getY() + d4 + 0.25;
                double particleZ = downPos.getZ() + 0.5;
                Direction hitDirection = value.getHitDirection();
                double edgeOffset = 0.1; // 外侧偏移量
                double yRandom = d0 * 0.4 + 0.3; // Y轴随机范围 [0.3, 0.7)
//                if (hitDirection != Direction.UP) return;
                //TODO 在大体积的情况下会漏
                switch (hitDirection) {
                    case UP -> {
                        // 顶部生成：XZ平面随机，Y保持顶部上方
                        particleX += d0 - 0.5; // [-0.5, 0.5) 范围
                        particleZ += d1 - 0.5;
                        particleY = downPos.getY() + 1.0 + edgeOffset;
                    }
                    case NORTH -> {
                        // 北侧生成：Z轴向外偏移，Y轴在碰撞高度附近随机
                        particleZ = downPos.getZ() - edgeOffset;
                        particleX += d0 - 0.5;
                        particleY += yRandom - 0.5; // 保持Y在碰撞高度附近
                    }
                    case SOUTH -> {
                        // 南侧生成：Z轴向外偏移
                        particleZ = downPos.getZ() + 1.0 + edgeOffset;
                        particleX += d0 - 0.5;
                        particleY += yRandom - 0.5;
                    }
                    case EAST -> {
                        // 东侧生成：X轴向外偏移
                        particleX = downPos.getX() + 1.0 + edgeOffset;
                        particleZ += d1 - 0.5;
                        particleY += yRandom - 0.5;
                    }
                    case WEST -> {
                        // 西侧生成：X轴向外偏移
                        particleX = downPos.getX() - edgeOffset;
                        particleZ += d1 - 0.5;
                        particleY += yRandom - 0.5;
                    }
                    case DOWN -> {
                        log.info("执行了");
                    }
                }
                levelreader.addParticle(
                        particleoptions,
                        particleX,
                        Mth.clamp(particleY, downPos.getY(), downPos.getY() + 1.0), // 限制Y在方块高度范围内
                        particleZ,
                        0.0, 0.0, 0.0
                );
//                if (BlockCollisionCheckerUtils.isInsideBlock(levelreader,downPos.getCenter(),false)) {
//                    log.info("位置:" + "x: " + particleX + " y: " + Mth.clamp(particleY, downPos.getY(), downPos.getY() + 1.0) + " z: " + particleZ);
//                }
            }
        });

    }

}
