package indi.yunherry.weather.util;

import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LevelUtils {
    // 最大有效坐标绝对值（根据Minecraft世界边界设定）
    private static final int MAX_COORDINATE = 30000000;
    // 最大渲染距离（单位：方块，可根据需求调整）
    private static final int RENDER_DISTANCE = 128;
    // 区块大小（标准Minecraft设置）
    private static final int CHUNK_SIZE = 16;

    /**
     * 判断坐标是否有效且需要渲染
     * @param player 玩家实体
     * @return 是否应该显示该方块
     */
    public static boolean shouldRenderBlock(BlockPos block, Player player) {
        // 1. 基础坐标有效性验证
        if (!isValidCoordinate(block.getX(), block.getY(), block.getZ(),player.level())) {
            return false;
        }

        // 2. 获取玩家位置
        Vec3 playerPos = player.position();

        // 3. 快速距离估算（避免开方运算）
        if (isBeyondFastDistance(block.getX(), block.getY(), block.getZ(), playerPos)) {
            return false;
        }

        // 4. 精确距离计算
        double distance = calculateExactDistance(block.getX(), block.getY(), block.getZ(), playerPos);
        if (distance > RENDER_DISTANCE) {
            return false;
        }

        // 5. 区块加载状态检查
        return isChunkLoaded(block.getX(),block.getZ(), player.level());
    }

    // 坐标有效性验证（包含Y轴限制）
    private static boolean isValidCoordinate(int x, int y, int z,Level level) {
        return Math.abs(x) <= MAX_COORDINATE &&
                Math.abs(z) <= MAX_COORDINATE &&
                y >= level.getMinBuildHeight() &&
                y <= level.getMaxBuildHeight();
    }

    // 快速距离估算（使用曼哈顿距离）
    private static boolean isBeyondFastDistance(int x, int y, int z, Vec3 playerPos) {
        int dx = (int)(playerPos.x - x);
        int dy = (int)(playerPos.y - y);
        int dz = (int)(playerPos.z - z);
        return (Math.abs(dx) + Math.abs(dy) + Math.abs(dz)) > (RENDER_DISTANCE * 3);
    }

    // 精确距离计算（欧几里得距离平方）
    private static double calculateExactDistance(int x, int y, int z, Vec3 playerPos) {
        double dx = playerPos.x - x;
        double dy = playerPos.y - y;
        double dz = playerPos.z - z;
        return dx*dx + dy*dy + dz*dz;
    }

    // 区块加载状态检查
    private static boolean isChunkLoaded(int x, int z, Level level) {
        int chunkX = x >> 4; // 等价于 x / 16
        int chunkZ = z >> 4;
        return level.hasChunk(chunkX, chunkZ);
    }


}
