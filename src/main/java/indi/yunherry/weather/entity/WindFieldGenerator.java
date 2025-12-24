package indi.yunherry.weather.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class WindFieldGenerator {

    // 分辨率
    private static final int CELL_SIZE = 4;

    public static ChunkFlowData generate(Level level, LevelChunk chunk) {
        int minHeight = level.getMinBuildHeight();
        int height = level.getHeight();
        int yCells = height / CELL_SIZE;
        int cellsPerRow = 16 / CELL_SIZE; // 4

        // 1. 初始化距离场 (存储每个 Cell 到最近出口的步数)
        int[][][] distMap = new int[cellsPerRow][yCells][cellsPerRow];
        for (int[][] plane : distMap) {
            for (int[] row : plane) {
                Arrays.fill(row, 9999); // 初始化为无穷大
            }
        }

        Queue<int[]> queue = new LinkedList<>();
        BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();

        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        // 2. 扫描种子点 (Source)
        // 这里的“Source”其实是风的终点(Sink)，即空旷地带/出口
        // 我们从出口开始反向寻路
        for (int y = 0; y < yCells; y++) {
            for (int x = 0; x < cellsPerRow; x++) {
                for (int z = 0; z < cellsPerRow; z++) {
                    // 取 Cell 中心点采样
                    int worldX = (chunkX << 4) + (x * CELL_SIZE) + 2;
                    int worldY = minHeight + (y * CELL_SIZE) + 2;
                    int worldZ = (chunkZ << 4) + (z * CELL_SIZE) + 2;

                    mutPos.set(worldX, worldY, worldZ);

                    // 判断是否是“开阔地带/出口”
                    // 条件：天空光照强 且 是空气
                    // 注意：这里需要 ensure chunk loaded，否则 getBrightness 可能导致死锁或加载错误
                    // 假设我们在 Client 端，且附近 chunk 已加载
                    if (level.getBlockState(mutPos).isAir()) {
                        int skyLight = level.getBrightness(LightLayer.SKY, mutPos);
                        if (skyLight >= 14) {
                            distMap[x][y][z] = 0;
                            queue.add(new int[]{x, y, z});
                        }
                    } else {
                        // 如果是固体方块，标记为不可达（或保留9999作为障碍物）
                        distMap[x][y][z] = -1; // -1 表示墙壁
                    }
                }
            }
        }

        // 3. BFS 泛洪计算距离
        int[][] directions = {{1,0,0}, {-1,0,0}, {0,1,0}, {0,-1,0}, {0,0,1}, {0,0,-1}};

        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int cx = curr[0];
            int cy = curr[1];
            int cz = curr[2];
            int currentDist = distMap[cx][cy][cz];

            for (int[] dir : directions) {
                int nx = cx + dir[0];
                int ny = cy + dir[1];
                int nz = cz + dir[2];

                // 边界检查
                if (nx >= 0 && nx < cellsPerRow &&
                        ny >= 0 && ny < yCells &&
                        nz >= 0 && nz < cellsPerRow) {

                    // 如果是墙壁(-1)或者已经找到更短路径，跳过
                    if (distMap[nx][ny][nz] == -1) continue;

                    if (distMap[nx][ny][nz] > currentDist + 1) {
                        distMap[nx][ny][nz] = currentDist + 1;
                        queue.add(new int[]{nx, ny, nz});
                    }
                }
            }
        }

        // 4. 根据距离场生成向量场 (Gradient)
        ChunkFlowData data = new ChunkFlowData(height, minHeight);

        for (int y = 0; y < yCells; y++) {
            for (int x = 0; x < cellsPerRow; x++) {
                for (int z = 0; z < cellsPerRow; z++) {
                    int myDist = distMap[x][y][z];

                    // 如果是墙壁或者是出口本身(0)，特殊处理
                    if (myDist == -1) continue; // 墙壁保持 0 向量
                    if (myDist == 0) {
                        // 已经是出口了，应用随机风或全局风
                        // 这里我们存一个特殊值或 0，渲染时交给 Noise 处理
                        continue;
                    }

                    // 寻找距离最小的邻居 (Gradient Descent)
                    double bestDx = 0;
                    double bestDy = 0;
                    double bestDz = 0;
                    int minDist = myDist;

                    for (int[] dir : directions) {
                        int nx = x + dir[0];
                        int ny = y + dir[1];
                        int nz = z + dir[2];

                        if (nx >= 0 && nx < cellsPerRow &&
                                ny >= 0 && ny < yCells &&
                                nz >= 0 && nz < cellsPerRow) {

                            int neighborDist = distMap[nx][ny][nz];
                            if (neighborDist != -1 && neighborDist < minDist) {
                                // 发现了一个通往出口更近的方向
                                bestDx += dir[0];
                                bestDy += dir[1];
                                bestDz += dir[2];
                                // 这里不更新 minDist，而是累加所有可行的“下坡”方向
                                // 这样风会顺滑地指向所有可行的出口方向
                            }
                        }
                    }

                    Vec3 flow = new Vec3(bestDx, bestDy, bestDz).normalize();
                    data.setVector(x, y, z, flow);
                }
            }
        }

        return data;
    }
}