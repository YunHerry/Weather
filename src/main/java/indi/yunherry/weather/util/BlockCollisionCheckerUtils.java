package indi.yunherry.weather.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockCollisionCheckerUtils {
    /**
     * 判断世界坐标是否在方块碰撞箱或流体内部
     * @param level 世界对象
     * @param worldPos 要检测的世界坐标（精确坐标）
     * @param checkFluid 是否检测流体
     * @return 是否在方块实体内部
     */
    public static boolean isInsideBlock(Level level, Vec3 worldPos, boolean checkFluid) {
        BlockPos blockPos = BlockPos.containing(worldPos);
        BlockState blockState = level.getBlockState(blockPos);

        // 快速排除空气方块
        if (blockState.isAir()) return false;

        // 转换为方块局部坐标（相对坐标）
        Vec3 localPos = new Vec3(
                worldPos.x - blockPos.getX(),
                worldPos.y - blockPos.getY(),
                worldPos.z - blockPos.getZ()
        );

        // 检测方块碰撞箱
        VoxelShape collisionShape = blockState.getCollisionShape(level, blockPos);
        if (!collisionShape.isEmpty()) {
            for (AABB aabb : collisionShape.toAabbs()) {
                if (aabb.contains(localPos)) {
                    return true;
                }
            }
        }

        // 检测流体高度
        if (checkFluid) {
            FluidState fluidState = blockState.getFluidState();
            if (!fluidState.isEmpty()) {
                float fluidHeight = fluidState.getHeight(level, blockPos);
                if (localPos.y <= fluidHeight) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 增强版检测（包含特殊方块处理）
     * @param level 世界对象
     * @param worldPos 世界坐标
     * @param checkFluid 是否检测流体
     * @param ignorePassable 是否忽略可穿透方块（如草、花）
     * @return 是否在有效方块内部
     */
    public static boolean isInsideBlockEnhanced(Level level, Vec3 worldPos,
                                                boolean checkFluid, boolean ignorePassable) {
        BlockPos blockPos = BlockPos.containing(worldPos);
        BlockState blockState = level.getBlockState(blockPos);

        // 处理特殊方块（如打开的活板门）
        if (blockState.hasProperty(BlockStateProperties.OPEN)) {
            if (blockState.getValue(BlockStateProperties.OPEN)) {
                return false;
            }
        }

        return isInsideBlock(level, worldPos, checkFluid);
    }
}
