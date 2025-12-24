package indi.yunherry.weather.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

public class ChunkFlowData {
    // 网格精度：每 4x4x4 个方块共享一个风向向量
    // 这大大减少了内存占用，同时对于“风”这种流体来说精度完全足够
    public static final int CELL_SIZE = 4;
    public static final int CELLS_PER_ROW = 16 / CELL_SIZE; // 4

    // 存储向量场：我们使用 byte 存储压缩的方向索引，或者直接存储 float xyz
    // 这里为了极致性能和简单，我们存 compact vector (x, y, z 压缩在 byte[] 里)
    // 数组大小：4 * 384(假设最大高度) * 4 / 4 = 很多...
    // 实际上我们需要按 Section 存储，或者只存由 y, x, z 索引的一维数组

    private final byte[] flowDirections; // 0: x, 1: y, 2: z (每个cell 3个byte)
    private final int minBuildHeight;
    private final int height;

    public ChunkFlowData(int height, int minBuildHeight) {
        this.height = height;
        this.minBuildHeight = minBuildHeight;
        // 计算总共有多少个 cell
        int yCells = height / CELL_SIZE;
        int totalCells = CELLS_PER_ROW * CELLS_PER_ROW * yCells;
        this.flowDirections = new byte[totalCells * 3]; // x, y, z components
    }

    public void setVector(int xCell, int yCell, int zCell, Vec3 vec) {
        int index = getIndex(xCell, yCell, zCell);
        if (index < 0 || index >= flowDirections.length - 2) return;

        // 将 float (-1.0 ~ 1.0) 压缩为 byte (-127 ~ 127)
        flowDirections[index]     = (byte) (vec.x * 127);
        flowDirections[index + 1] = (byte) (vec.y * 127);
        flowDirections[index + 2] = (byte) (vec.z * 127);
    }

    public Vec3 getVector(int blockX, int blockY, int blockZ) {
        int localX = blockX & 15;
        int localZ = blockZ & 15;
        // 映射到 Cell 坐标
        int xCell = localX / CELL_SIZE;
        int yCell = (blockY - minBuildHeight) / CELL_SIZE;
        int zCell = localZ / CELL_SIZE;

        int index = getIndex(xCell, yCell, zCell);

        // 越界检查（比如太高或太低）
        if (index < 0 || index >= flowDirections.length - 2) {
            return Vec3.ZERO;
        }

        double vx = flowDirections[index] / 127.0;
        double vy = flowDirections[index + 1] / 127.0;
        double vz = flowDirections[index + 2] / 127.0;

        return new Vec3(vx, vy, vz);
    }

    private int getIndex(int x, int y, int z) {
        // 简单的扁平化索引
        int yCells = height / CELL_SIZE;
        if (y < 0 || y >= yCells) return -1;
        return ((y * CELLS_PER_ROW + z) * CELLS_PER_ROW + x) * 3;
    }
}