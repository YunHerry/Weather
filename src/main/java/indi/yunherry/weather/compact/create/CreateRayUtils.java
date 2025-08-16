package indi.yunherry.weather.compact.create;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class CreateRaycastSystem {

    /**
     * 带动态结构信息的碰撞结果
     */
    public static class ContraptionHitResult extends BlockHitResult {
        public final Vec3 contactPointMotion;
        public final AbstractContraptionEntity contraption;
        public final Vec3 localHitPosition;
        public final BlockState actualBlockState;

        public ContraptionHitResult(Vec3 contactPointMotion, Vec3 location, Direction direction,
                                    BlockPos blockPos, boolean inside, AbstractContraptionEntity contraption,
                                    Vec3 localHitPosition, BlockState actualBlockState) {
            super(location, direction, blockPos, inside);
            this.contactPointMotion = contactPointMotion;
            this.contraption = contraption;
            this.localHitPosition = localHitPosition;
            this.actualBlockState = actualBlockState;
        }
    }

    /**
     * 主要的射线检测方法：检测射线与原版世界 + Create 动态结构的碰撞
     * 基于AsyncParticles的CreateUtils.clip方法实现
     */
    public static BlockHitResult clipWithContraptions(ClientLevel level, Vec3 start, Vec3 end,
                                                      ClipContext.Block blockMode,
                                                      ClipContext.Fluid fluidMode) {
        // 1. 执行原版射线检测
        ClipContext vanillaContext = new ClipContext(start, end, blockMode, fluidMode, null);
        BlockHitResult vanillaHit = level.clip(vanillaContext);

        BlockHitResult closestHit = vanillaHit;
        //原版最近距离
        double closestDistance = vanillaHit.getLocation().distanceToSqr(start);

        double shortestDistance = Double.MAX_VALUE;
        BlockHitResult contraptionResult = null;
        Vec3 contraptionHitPos = null;
        AbstractContraptionEntity hitContraption = null;

        // 遍历所有动态结构
        for (Iterator<AbstractContraptionEntity> it = CreateUtils.forEachContraption(level); it.hasNext(); ) {
            AbstractContraptionEntity entity = it.next();


            // 快速包围盒检查
            AABB entityBb = entity.getBoundingBox();
            if (!entityBb.intersects(start, end)) {
                continue;
            }

            // 使用Create原生的射线检测方法
            BlockHitResult hitResult = ContraptionHandlerClient.rayTraceContraption(start, end, entity);

            if (hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
                // 将本地坐标转换为世界坐标
                Vec3 worldHit = entity.toGlobalVector(hitResult.getLocation(), 1.0F);
                double hitDistance = start.distanceToSqr(worldHit);

                if (hitDistance < shortestDistance) {
                    shortestDistance = hitDistance;
                    contraptionResult = hitResult;
                    contraptionHitPos = worldHit;
                    hitContraption = entity;
                }
            }
        }

        // 3. 比较原版结果和动态结构结果
        if (contraptionResult != null && contraptionHitPos != null) {
            double contraptionDistance = contraptionHitPos.distanceToSqr(start);
            if (contraptionDistance < closestDistance) {
                // 创建增强的碰撞结果
                BlockState actualBlockState = getActualBlockState(hitContraption, contraptionResult.getBlockPos());
                Vec3 contactPointMotion = hitContraption.getContactPointMotion(contraptionHitPos);

                return new ContraptionHitResult(
                        contactPointMotion,
                        contraptionHitPos,
                        contraptionResult.getDirection(),
                        CreateUtils.localToWorldBlockPos(hitContraption,contraptionResult.getBlockPos()),
                        contraptionResult.isInside(),
                        hitContraption,
                        contraptionResult.getLocation(),
                        actualBlockState
                );
            }
        }

        return closestHit;
    }

    /**
     * 便捷方法：使用默认碰撞模式进行射线检测
     */
    public static BlockHitResult clipWithContraptions(ClientLevel level, Vec3 start, Vec3 end) {
        return clipWithContraptions(level, start, end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE);
    }


    /**
     * 获取动态结构中指定位置的实际方块状态
     */
    private static BlockState getActualBlockState(AbstractContraptionEntity contraptionEntity, BlockPos localPos) {
        try {
            Contraption contraption = contraptionEntity.getContraption();
            Map<BlockPos, StructureTemplate.StructureBlockInfo> blocks = contraption.getBlocks();

            StructureTemplate.StructureBlockInfo blockInfo = blocks.get(localPos);
            if (blockInfo != null) {
                BlockState blockState = blockInfo.state();
                // 确保不是air或void_air
                if (blockState != null && !blockState.isAir() && blockState.getBlock() != Blocks.VOID_AIR) {
                    return blockState;
                }
            }

            // 尝试从ContraptionWorld获取
            if (contraption.getContraptionWorld() != null) {
                BlockState worldState = contraption.getContraptionWorld().getBlockState(localPos);
                if (worldState != null && !worldState.isAir() && worldState.getBlock() != Blocks.VOID_AIR) {
                    return worldState;
                }
            }

            return null;
        } catch (Exception e) {
            System.err.println("获取方块状态失败: " + e.getMessage());
            return null;
        }
    }


}