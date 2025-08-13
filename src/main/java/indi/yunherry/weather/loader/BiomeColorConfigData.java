package indi.yunherry.weather.loader;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * 生物群系颜色配置的记录类
 * 用于从 JSON 配置文件中反序列化颜色数据
 */
public record BiomeColorConfigData(
        @SerializedName("default_color") String defaultColor,
        @SerializedName("loader") String loader,
        @SerializedName("step") int step,
        @SerializedName("data") Map<String, BiomeColorData> data
) {

    // 便捷方法：获取特定生物群系的颜色数据
    public BiomeColorData getBiomeColorData(String biomeId) {
        return data != null ? data.get(biomeId) : new BiomeColorData(defaultColor,defaultColor,defaultColor);
    }

    // 便捷方法：检查是否包含特定生物群系
    public boolean hasBiome(String biomeId) {
        return data != null && data.containsKey(biomeId);
    }

    /**
     * 内部记录类：生物群系颜色数据
     * 包含起始颜色、中间颜色和结束颜色
     */
    public record BiomeColorData(
            @SerializedName("startColor") String startColor,
            @SerializedName("midColor") String midColor,
            @SerializedName("endColor") String endColor
    ) {

        // 便捷方法：将颜色字符串转换为整数
        public int getStartColorAsInt() {
            return parseColorString(startColor);
        }

        public int getMidColorAsInt() {
            return parseColorString(midColor);
        }

        public int getEndColorAsInt() {
            return parseColorString(endColor);
        }

        // 解析颜色字符串 (#RRGGBB) 为整数
        private int parseColorString(String colorStr) {
            if (colorStr == null || !colorStr.startsWith("#")) {
                return 0;
            }
            try {
                return Integer.parseInt(colorStr.substring(1), 16);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }
}