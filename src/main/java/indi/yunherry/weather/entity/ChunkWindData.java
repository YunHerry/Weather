package indi.yunherry.weather.entity;

public class ChunkWindData {
    private final float[] sectionDensities;
    private final int minBuildHeight;

    public ChunkWindData(int sectionCount, int minBuildHeight) {
        this.sectionDensities = new float[sectionCount];
        this.minBuildHeight = minBuildHeight;
    }

    public void setDensity(int sectionIndex, float density) {
        if (sectionIndex >= 0 && sectionIndex < sectionDensities.length) {
            sectionDensities[sectionIndex] = density;
        }
    }

    /**
     * 获取指定 Y 坐标所在的 Section 的空气密度
     */
    public float getDensityAtY(int y) {
        int sectionIndex = (y - minBuildHeight) >> 4; // 相当于除以 16
        if (sectionIndex >= 0 && sectionIndex < sectionDensities.length) {
            return sectionDensities[sectionIndex];
        }
        return 0f; // 超出范围默认无风
    }

    public float[] getAllDensities() {
        return sectionDensities;
    }

}
